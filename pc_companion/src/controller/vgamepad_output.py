from typing import Dict, Any, Optional
from .base import AbstractGamepad
from ..protocol import InputPacket

try:
    import vgamepad as vg
    HAS_VGAMEPAD = True
except (ImportError, Exception):
    vg = None
    HAS_VGAMEPAD = False

class VGamepadOutput(AbstractGamepad):
    """
    Virtual Xbox 360 Gamepad integration using ViGEmBus driver on Windows 10/11.
    """

    def __init__(self, mappings: Dict[str, str]):
        if not HAS_VGAMEPAD:
            raise RuntimeError(
                "vgamepad is not installed or ViGEmBus driver is not available. "
                "Install ViGEmBus and run 'pip install vgamepad'."
            )

        self.mappings = mappings
        self.gamepad = vg.VX360Gamepad()
        self._is_active = True
        self.last_packet = InputPacket.neutral()

        # Map string button names to vgamepad XUSB_BUTTON constants
        self._button_map = {
            "A": vg.XUSB_BUTTON.XUSB_GAMEPAD_A,
            "B": vg.XUSB_BUTTON.XUSB_GAMEPAD_B,
            "X": vg.XUSB_BUTTON.XUSB_GAMEPAD_X,
            "Y": vg.XUSB_BUTTON.XUSB_GAMEPAD_Y,
            "LEFT_SHOULDER": vg.XUSB_BUTTON.XUSB_GAMEPAD_LEFT_SHOULDER,
            "RIGHT_SHOULDER": vg.XUSB_BUTTON.XUSB_GAMEPAD_RIGHT_SHOULDER,
            "START": vg.XUSB_BUTTON.XUSB_GAMEPAD_START,
            "BACK": vg.XUSB_BUTTON.XUSB_GAMEPAD_BACK,
            "LEFT_THUMB": vg.XUSB_BUTTON.XUSB_GAMEPAD_LEFT_THUMB,
            "RIGHT_THUMB": vg.XUSB_BUTTON.XUSB_GAMEPAD_RIGHT_THUMB
        }

    def _set_button(self, button_name: str, pressed: bool):
        btn = self._button_map.get(button_name)
        if btn is not None:
            if pressed:
                self.gamepad.press_button(button=btn)
            else:
                self.gamepad.release_button(button=btn)

    def apply_inputs(self, packet: InputPacket) -> None:
        if not self._is_active:
            return

        self.last_packet = packet

        # 1. Analog Steering (Left Stick X)
        self.gamepad.left_joystick_float(x_value_float=packet.steering, y_value_float=0.0)

        # 2. Analog Throttle (Right Trigger: 0.0 to 1.0)
        self.gamepad.right_trigger_float(value_float=packet.throttle)

        # 3. Analog Brake (Left Trigger: 0.0 to 1.0)
        self.gamepad.left_trigger_float(value_float=packet.brake)

        # 4. Buttons according to mapping
        self._set_button(self.mappings.get("handbrake_button", "A"), packet.handbrake)
        self._set_button(self.mappings.get("gear_up_button", "RIGHT_SHOULDER"), packet.gear_up)
        self._set_button(self.mappings.get("gear_down_button", "LEFT_SHOULDER"), packet.gear_down)
        self._set_button(self.mappings.get("clutch_button", "X"), packet.clutch > 0.5)
        self._set_button(self.mappings.get("select_button", "BACK"), packet.select)
        self._set_button(self.mappings.get("start_button", "START"), packet.start)

        # Push state to Windows virtual device
        self.gamepad.update()

    def reset_all(self) -> None:
        if not self._is_active:
            return

        self.gamepad.reset()
        self.gamepad.update()
        self.last_packet = InputPacket.neutral()

    def is_active(self) -> bool:
        return self._is_active

    def get_status(self) -> Dict[str, Any]:
        return {
            "type": "ViGEm Virtual Xbox 360 Controller",
            "driver_ready": True,
            "is_mock": False,
            "last_inputs": self.last_packet.to_dict()
        }

    def close(self) -> None:
        self.reset_all()
        self._is_active = False
        del self.gamepad
