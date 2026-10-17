import platform
from typing import Dict
from .base import AbstractGamepad
from .mock_output import MockGamepad

def create_gamepad(mappings: Dict[str, str], force_mock: bool = False) -> AbstractGamepad:
    """
    Factory function to instantiate the best available virtual gamepad.
    On Windows with ViGEmBus installed, uses VGamepadOutput (Xbox 360 controller).
    Otherwise, cleanly falls back to MockGamepad with clear console logging.
    """
    if force_mock or platform.system() != "Windows":
        return MockGamepad(mappings)

    try:
        from .vgamepad_output import VGamepadOutput
        return VGamepadOutput(mappings)
    except Exception as e:
        print(f"[*] ViGEmBus virtual gamepad unavailable ({e}). Falling back to MockGamepad.")
        return MockGamepad(mappings)
