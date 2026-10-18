import unittest
import asyncio
from unittest.mock import patch, MagicMock
import os
import sys

current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
if parent_dir not in sys.path:
    sys.path.insert(0, parent_dir)

from src.usb_manager import AdbUsbManager

class TestAdbUsbManager(unittest.TestCase):
    def setUp(self):
        self.mgr = AdbUsbManager(port=9876, vibration_port=9877)

    def test_init_properties(self):
        self.assertEqual(self.mgr.port, 9876)
        self.assertEqual(self.mgr.vibration_port, 9877)
        self.assertFalse(self.mgr.reverse_active)
        status = self.mgr.get_status()
        self.assertIn("adb_available", status)
        self.assertIn("connected_devices", status)
        self.assertIn("reverse_active", status)

    def test_adb_binary_detection(self):
        # We know adb is installed on this environment (/usr/bin/adb)
        adb = self.mgr._find_adb_binary()
        self.assertIsNotNone(adb)

    def test_parse_devices(self):
        async def run_test():
            fake_stdout = b"List of devices attached\nRF8R31XXXXX\tdevice\n127.0.0.1:5555\tunauthorized\nemulator-5554\tdevice\n\n"
            mock_proc = MagicMock()
            mock_proc.communicate = MagicMock(return_value=asyncio.sleep(0, result=(fake_stdout, b"")))
            with patch("asyncio.create_subprocess_exec", return_value=mock_proc):
                devs = await self.mgr.get_connected_devices()
                self.assertEqual(len(devs), 2)
                self.assertIn("RF8R31XXXXX", devs)
                self.assertIn("emulator-5554", devs)

        asyncio.run(run_test())

    def test_setup_reverse_success(self):
        async def run_test():
            mock_proc = MagicMock()
            mock_proc.returncode = 0
            mock_proc.communicate = MagicMock(side_effect=lambda: asyncio.sleep(0, result=(b"9876", b"")))
            with patch("asyncio.create_subprocess_exec", return_value=mock_proc):
                ok = await self.mgr.setup_reverse()
                self.assertTrue(ok)
                self.assertTrue(self.mgr.reverse_active)
                self.assertIn("Active", self.mgr.last_status)

        asyncio.run(run_test())

    def test_remove_reverse(self):
        async def run_test():
            mock_proc = MagicMock()
            mock_proc.communicate = MagicMock(side_effect=lambda: asyncio.sleep(0, result=(b"", b"")))
            with patch("asyncio.create_subprocess_exec", return_value=mock_proc):
                self.mgr.reverse_active = True
                ok = await self.mgr.remove_reverse()
                self.assertTrue(ok)
                self.assertFalse(self.mgr.reverse_active)

        asyncio.run(run_test())

if __name__ == "__main__":
    unittest.main()
