from typing import Dict, Any
from .base import AbstractGamepad
from ..protocol import InputPacket

class MockGamepad(AbstractGamepad):
    """
    Mock virtual gamepad for development, testing, and Linux environments.
    Simulates axes and button tracking without requiring the Windows ViGEmBus driver.
    """

    def __init__(self, mappings: Dict[str, str]):
        self.mappings = mappings
        self._is_active = True
        self.last_packet = InputPacket.neutral()
        self.updates_count = 0
        self.reset_count = 0

    def apply_inputs(self, packet: InputPacket) -> None:
        self.last_packet = packet
        self.updates_count += 1

    def reset_all(self) -> None:
        self.last_packet = InputPacket.neutral()
        self.reset_count += 1

    def is_active(self) -> bool:
        return self._is_active

    def get_status(self) -> Dict[str, Any]:
        return {
            "type": "Mock / Simulated Virtual Gamepad",
            "driver_ready": True,
            "is_mock": True,
            "updates_count": self.updates_count,
            "reset_count": self.reset_count,
            "last_inputs": self.last_packet.to_dict()
        }

    def close(self) -> None:
        self.reset_all()
        self._is_active = False
