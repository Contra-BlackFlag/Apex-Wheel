import asyncio
import os
import shutil
import sys
from typing import List, Dict, Any, Optional

class AdbUsbManager:
    """
    Manages wired USB cable connectivity using Android Debug Bridge (ADB) reverse port forwarding.
    Automatically discovers plugged-in Android devices and maps ports 9876 and 9877 over the USB cable
    for sub-millisecond round-trip latency (<1ms) and zero wireless interference.
    """

    def __init__(self, port: int = 9876, vibration_port: int = 9877):
        self.port = port
        self.vibration_port = vibration_port
        self.adb_path = self._find_adb_binary()
        self.connected_devices: List[str] = []
        self.reverse_active: bool = False
        self.last_status: str = "Initializing"
        self.last_error: Optional[str] = None
        self._watcher_task: Optional[asyncio.Task] = None
        self._running: bool = False

    def _find_adb_binary(self) -> Optional[str]:
        """Locates the adb executable across system PATH and standard SDK installation paths."""
        found = shutil.which("adb")
        if found:
            return found

        # Windows-specific standard paths
        if sys.platform == "win32":
            candidates = [
                os.path.expandvars(r"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"),
                os.path.expandvars(r"%PROGRAMFILES%\Android\platform-tools\adb.exe"),
                os.path.expandvars(r"%PROGRAMFILES(X86)%\Android\platform-tools\adb.exe"),
                r"C:\platform-tools\adb.exe",
                r"C:\adb\adb.exe"
            ]
            for candidate in candidates:
                if os.path.exists(candidate):
                    return candidate

        # Linux / macOS standard paths
        candidates = [
            os.path.expanduser("~/Android/Sdk/platform-tools/adb"),
            "/usr/bin/adb",
            "/usr/local/bin/adb",
            "/opt/android-sdk/platform-tools/adb"
        ]
        for candidate in candidates:
            if os.path.exists(candidate):
                return candidate

        return None

    async def get_connected_devices(self) -> List[str]:
        """Runs 'adb devices' and returns a list of authorized device serials."""
        if not self.adb_path:
            return []

        try:
            proc = await asyncio.create_subprocess_exec(
                self.adb_path, "devices",
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            stdout, _ = await asyncio.wait_for(proc.communicate(), timeout=3.0)
            lines = stdout.decode("utf-8", errors="ignore").splitlines()
            
            devices = []
            for line in lines[1:]:  # Skip "List of devices attached"
                line = line.strip()
                if not line:
                    continue
                parts = line.split()
                if len(parts) >= 2 and parts[1] == "device":
                    devices.append(parts[0])
            return devices
        except Exception as e:
            self.last_error = str(e)
            return []

    async def setup_reverse(self) -> bool:
        """Sets up adb reverse port forwarding for both controller input and vibration."""
        if not self.adb_path:
            self.last_status = "ADB binary not found"
            return False

        try:
            # 1. Reverse controller input port (9876)
            proc_input = await asyncio.create_subprocess_exec(
                self.adb_path, "reverse", f"tcp:{self.port}", f"tcp:{self.port}",
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            out_in, err_in = await asyncio.wait_for(proc_input.communicate(), timeout=3.0)

            # 2. Reverse vibration port (9877) if configured
            reverse_vibe_ok = True
            if self.vibration_port:
                proc_vibe = await asyncio.create_subprocess_exec(
                    self.adb_path, "reverse", f"tcp:{self.vibration_port}", f"tcp:{self.vibration_port}",
                    stdout=asyncio.subprocess.PIPE,
                    stderr=asyncio.subprocess.PIPE
                )
                out_vibe, err_vibe = await asyncio.wait_for(proc_vibe.communicate(), timeout=3.0)
                reverse_vibe_ok = (proc_vibe.returncode == 0)

            if proc_input.returncode == 0 and reverse_vibe_ok:
                self.reverse_active = True
                self.last_status = f"ADB Reverse Active (Ports {self.port}, {self.vibration_port})"
                self.last_error = None
                return True
            else:
                err_msg = (err_in.decode("utf-8", errors="ignore")).strip()
                self.last_status = f"ADB Reverse Failed: {err_msg}"
                self.last_error = err_msg
                return False
        except Exception as e:
            self.last_status = f"Error: {e}"
            self.last_error = str(e)
            return False

    async def remove_reverse(self) -> bool:
        """Removes the reversed ports from adb."""
        if not self.adb_path:
            return False

        try:
            proc = await asyncio.create_subprocess_exec(
                self.adb_path, "reverse", "--remove", f"tcp:{self.port}",
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            await asyncio.wait_for(proc.communicate(), timeout=2.0)
            if self.vibration_port:
                proc2 = await asyncio.create_subprocess_exec(
                    self.adb_path, "reverse", "--remove", f"tcp:{self.vibration_port}",
                    stdout=asyncio.subprocess.PIPE,
                    stderr=asyncio.subprocess.PIPE
                )
                await asyncio.wait_for(proc2.communicate(), timeout=2.0)
            self.reverse_active = False
            return True
        except Exception:
            return False

    async def start(self):
        """Starts the background USB device monitoring loop."""
        if not self.adb_path:
            self.last_status = "ADB not installed (Install Android SDK or Platform Tools for USB auto-forwarding)"
            return

        self._running = True
        self._watcher_task = asyncio.create_task(self._watch_loop())

    async def stop(self):
        """Stops the watcher loop and cleans up adb reverse mappings."""
        self._running = False
        if self._watcher_task:
            self._watcher_task.cancel()
            try:
                await self._watcher_task
            except asyncio.CancelledError:
                pass
        await self.remove_reverse()

    async def _watch_loop(self):
        """Periodically polls for USB devices and maintains adb reverse bindings."""
        had_devices = False
        while self._running:
            try:
                devices = await self.get_connected_devices()
                self.connected_devices = devices

                if devices and not had_devices:
                    print(f"\n[+] 🔌 USB Android Device Detected: {', '.join(devices)}")
                    success = await self.setup_reverse()
                    if success:
                        print(f"[*] ⚡ USB Wired Tunnel Active! Phone can now connect directly to 127.0.0.1 (Latency < 0.5ms)")
                    had_devices = True
                elif not devices and had_devices:
                    print("\n[-] 🔌 USB Android Device Disconnected.")
                    self.reverse_active = False
                    self.last_status = "Waiting for USB device"
                    had_devices = False
                elif devices and not self.reverse_active:
                    await self.setup_reverse()
            except asyncio.CancelledError:
                break
            except Exception as e:
                self.last_error = str(e)

            await asyncio.sleep(2.5)

    def get_status(self) -> Dict[str, Any]:
        """Returns structured status for Web Control Panel and terminal diagnostics."""
        return {
            "adb_available": self.adb_path is not None,
            "adb_path": self.adb_path,
            "connected_devices": self.connected_devices,
            "reverse_active": self.reverse_active,
            "status": self.last_status,
            "has_device": len(self.connected_devices) > 0,
            "last_error": self.last_error
        }
