import asyncio
import signal
import sys
from typing import Dict, Any

from .config import CompanionConfig
from .controller import create_gamepad
from .server import ControllerServer
from .ui.web_ui import WebDashboardServer
from .ui.cli_ui import TerminalUI

class CompanionApplication:
    def __init__(
        self,
        config_file: str = "config.json",
        force_mock: bool = False,
        port_override: int = None,
        web_port_override: int = None
    ):
        self.config = CompanionConfig(config_file)
        if port_override is not None:
            self.config.data["server"]["port"] = port_override
        if web_port_override is not None:
            self.config.data["server"]["web_port"] = web_port_override

        self.gamepad = create_gamepad(self.config.mappings, force_mock=force_mock)
        self.local_ips = CompanionConfig.get_local_ip_addresses()

        self.server = ControllerServer(
            host=self.config.host,
            port=self.config.port,
            gamepad=self.gamepad,
            watchdog_timeout_seconds=self.config.watchdog_timeout_seconds
        )

        self.web_server = WebDashboardServer(
            host=self.config.host,
            port=self.config.web_port,
            get_state_callback=self._get_combined_state
        )

        self._running = False

    def _get_combined_state(self) -> Dict[str, Any]:
        metrics = self.server.get_metrics()
        metrics["local_ips"] = self.local_ips
        metrics["port"] = self.config.port
        metrics["web_port"] = self.config.web_port
        return metrics

    async def run(self):
        self._running = True

        print("==================================================")
        print("          APEXWHEEL RACING COMPANION              ")
        print("==================================================")
        print(f"[*] Virtual Gamepad Driver: {self.gamepad.get_status()['type']}")
        print(f"[*] Controller Port (Wi-Fi): {self.config.port}")
        print(f"[*] Web Dashboard Port:      http://localhost:{self.config.web_port}")
        print("[*] Available Local IP Addresses:")
        for ip in self.local_ips:
            print(f"     -> {ip}")
        print("--------------------------------------------------")
        print("[*] Enter one of the above IP addresses into your")
        print("    ApexWheel Android Controller app.")
        print("==================================================")

        await self.server.start()
        await self.web_server.start()

        # Terminal HUD loop
        last_line_len = 0
        try:
            while self._running:
                await asyncio.sleep(0.2)
                metrics = self.server.get_metrics()
                if metrics["connected"]:
                    hud_line = TerminalUI.format_status_line(metrics)
                    sys.stdout.write("\r" + hud_line + (" " * max(0, last_line_len - len(hud_line))))
                    sys.stdout.flush()
                    last_line_len = len(hud_line)
        except asyncio.CancelledError:
            pass
        finally:
            await self.shutdown()

    async def shutdown(self):
        print("\n[*] Shutting down ApexWheel companion...")
        self._running = False
        await self.server.stop()
        await self.web_server.stop()
        self.gamepad.close()
        print("[*] Shutdown complete.")

def main():
    import argparse
    parser = argparse.ArgumentParser(description="ApexWheel PC Companion")
    parser.add_argument("--config", default="config.json", help="Path to config.json")
    parser.add_argument("--mock", action="store_true", help="Force mock gamepad even on Windows")
    parser.add_argument("--port", type=int, default=None, help="Override controller port")
    parser.add_argument("--web-port", type=int, default=None, help="Override web UI port")
    args = parser.parse_args()

    app = CompanionApplication(
        config_file=args.config,
        force_mock=args.mock,
        port_override=args.port,
        web_port_override=args.web_port
    )

    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)

    for sig in (signal.SIGINT, signal.SIGTERM):
        try:
            loop.add_signal_handler(sig, lambda: asyncio.create_task(app.shutdown()))
        except (NotImplementedError, AttributeError):
            # Windows signals are handled differently
            pass

    try:
        loop.run_until_complete(app.run())
    except KeyboardInterrupt:
        loop.run_until_complete(app.shutdown())
    finally:
        loop.close()

if __name__ == "__main__":
    main()
