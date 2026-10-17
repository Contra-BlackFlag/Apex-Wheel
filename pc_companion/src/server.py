import asyncio
import time
from typing import Optional, Dict, Any, Callable
from .protocol import parse_packet, create_pong_packet, InputPacket, ProtocolError
from .controller.base import AbstractGamepad

class ControllerServer:
    def __init__(
        self,
        host: str,
        port: int,
        gamepad: AbstractGamepad,
        watchdog_timeout_seconds: float = 0.5,
        on_state_change: Optional[Callable[[Dict[str, Any]], None]] = None
    ):
        self.host = host
        self.port = port
        self.gamepad = gamepad
        self.watchdog_timeout = watchdog_timeout_seconds
        self.on_state_change = on_state_change

        self.server: Optional[asyncio.Server] = None
        self.active_writer: Optional[asyncio.StreamWriter] = None
        self.client_address: Optional[str] = None
        self.last_packet_time: float = 0.0
        self.total_packets: int = 0
        self.watchdog_task: Optional[asyncio.Task] = None
        self.is_running: bool = False

        # Live metric tracking
        self._rate_counter: int = 0
        self._last_rate_check: float = time.time()
        self.current_rate_hz: float = 0.0
        self.last_inputs = InputPacket.neutral()

    async def start(self):
        """Starts the TCP socket server."""
        self.is_running = True
        self.server = await asyncio.start_server(
            self._handle_client,
            self.host,
            self.port
        )
        self.watchdog_task = asyncio.create_task(self._watchdog_loop())
        print(f"[*] Racing Controller Server listening on {self.host}:{self.port}")

    async def stop(self):
        """Stops the server, disconnects clients, and safely resets controller."""
        self.is_running = False
        if self.watchdog_task:
            self.watchdog_task.cancel()

        if self.active_writer:
            try:
                self.active_writer.close()
                await self.active_writer.wait_closed()
            except Exception:
                pass

        if self.server:
            self.server.close()
            await self.server.wait_closed()

        self.gamepad.reset_all()
        print("[*] Server stopped. All controller axes and buttons reset to neutral.")

    async def _handle_client(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter):
        addr = writer.get_extra_info("peername")
        client_ip = f"{addr[0]}:{addr[1]}" if addr else "unknown"

        # Enforce single client policy: close existing or reject
        if self.active_writer is not None:
            print(f"[!] New client connection from {client_ip}. Replacing existing connection.")
            try:
                self.active_writer.close()
            except Exception:
                pass

        self.active_writer = writer
        self.client_address = client_ip
        self.last_packet_time = time.time()

        # Ultra-low latency optimization: disable Nagle's algorithm and reduce bufferbloat
        sock = writer.get_extra_info("socket")
        if sock is not None:
            try:
                import socket as sock_mod
                sock.setsockopt(sock_mod.IPPROTO_TCP, sock_mod.TCP_NODELAY, 1)
                sock.setsockopt(sock_mod.SOL_SOCKET, sock_mod.SO_RCVBUF, 4096)
            except Exception:
                pass

        print(f"[+] Client connected from {client_ip} (TCP_NODELAY active)")

        self._notify_state()

        try:
            while self.is_running:
                line = await reader.readline()
                if not line:
                    break

                self.last_packet_time = time.time()
                self.total_packets += 1
                self._rate_counter += 1

                # Calculate Hz rate every 0.5s
                now = time.time()
                elapsed = now - self._last_rate_check
                if elapsed >= 0.5:
                    self.current_rate_hz = round(self._rate_counter / elapsed, 1)
                    self._rate_counter = 0
                    self._last_rate_check = now

                line_str = line.decode("utf-8", errors="ignore").strip()
                if not line_str:
                    continue

                try:
                    msg_type, payload = parse_packet(line_str)
                    if msg_type == "input" and isinstance(payload, InputPacket):
                        self.last_inputs = payload
                        self.gamepad.apply_inputs(payload)
                        self._notify_state()
                    elif msg_type == "ping" and isinstance(payload, int):
                        pong_bytes = create_pong_packet(payload).encode("utf-8")
                        writer.write(pong_bytes)
                        await writer.drain()
                except ProtocolError as pe:
                    # Ignore malformed packets without crashing the session
                    pass

        except (ConnectionResetError, asyncio.IncompleteReadError):
            pass
        except Exception as e:
            print(f"[!] Client error: {e}")
        finally:
            print(f"[-] Client {client_ip} disconnected.")
            if self.active_writer == writer:
                self.active_writer = None
                self.client_address = None
                self.current_rate_hz = 0.0

            # CRITICAL DISCONNECT SAFETY: Immediately reset all inputs
            self.gamepad.reset_all()
            self.last_inputs = InputPacket.neutral()
            self._notify_state()

    async def _watchdog_loop(self):
        """Monitors for heartbeat / packet timeout and triggers safety neutral reset."""
        while self.is_running:
            await asyncio.sleep(0.05) # Check every 50ms
            if self.active_writer is not None:
                idle_time = time.time() - self.last_packet_time
                if idle_time > self.watchdog_timeout:
                    # Signal lost! Trigger emergency neutral
                    self.gamepad.reset_all()
                    self.last_inputs = InputPacket.neutral()
                    self.current_rate_hz = 0.0
                    self._notify_state()

    def _notify_state(self):
        if self.on_state_change:
            try:
                self.on_state_change(self.get_metrics())
            except Exception:
                pass

    def get_metrics(self) -> Dict[str, Any]:
        return {
            "connected": self.active_writer is not None,
            "client_address": self.client_address,
            "total_packets": self.total_packets,
            "rate_hz": self.current_rate_hz,
            "inputs": self.last_inputs.to_dict(),
            "gamepad": self.gamepad.get_status()
        }
