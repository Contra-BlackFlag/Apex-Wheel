from abc import ABC, abstractmethod
from typing import Dict, Any
from ..protocol import InputPacket

class AbstractGamepad(ABC):
    """Abstract interface for PC Gamepad outputs (ViGEm Xbox 360, Mock, etc.)"""

    @abstractmethod
    def apply_inputs(self, packet: InputPacket) -> None:
        """Applies input values to the virtual controller axes and buttons."""
        pass

    @abstractmethod
    def reset_all(self) -> None:
        """Failsafe: releases all buttons and resets all axes to neutral (center / 0)."""
        pass

    @abstractmethod
    def is_active(self) -> bool:
        """Returns True if the virtual device driver is initialized and operational."""
        pass

    @abstractmethod
    def get_status(self) -> Dict[str, Any]:
        """Returns current device status, state, and driver type for UI reporting."""
        pass

    @abstractmethod
    def close(self) -> None:
        """Releases the virtual gamepad and unplugs from the virtual bus."""
        pass
