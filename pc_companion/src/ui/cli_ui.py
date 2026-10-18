import os
import sys
import time
from typing import Dict, Any

class TerminalUI:
    """Renders a compact ANSI dashboard in terminal."""

    @staticmethod
    def render_meter(val: float, width: int = 14, is_bidirectional: bool = False) -> str:
        if is_bidirectional:
            # -1.0 to 1.0
            mid = width // 2
            pos = int((val + 1.0) / 2.0 * width)
            pos = max(0, min(width - 1, pos))
            bar = [" "] * width
            bar[mid] = "|"
            bar[pos] = "█"
            return "[" + "".join(bar) + "]"
        else:
            # 0.0 to 1.0
            filled = int(val * width)
            filled = max(0, min(width, filled))
            return "[" + ("█" * filled) + ("-" * (width - filled)) + "]"

    @classmethod
    def format_status_line(cls, metrics: Dict[str, Any]) -> str:
        connected = metrics.get("connected", False)
        status_str = "\033[92mCONNECTED\033[0m" if connected else "\033[91mWAITING\033[0m"
        inputs = metrics.get("inputs", {})
        rate = metrics.get("rate_hz", 0.0)

        steer = inputs.get("steering", 0.0)
        throttle = inputs.get("throttle", 0.0)
        brake = inputs.get("brake", 0.0)
        clutch = inputs.get("clutch", 0.0)

        steer_bar = cls.render_meter(steer, width=12, is_bidirectional=True)
        throt_bar = cls.render_meter(throttle, width=8)
        brake_bar = cls.render_meter(brake, width=8)

        btns = []
        if inputs.get("handbrake"): btns.append("E-BRK")
        if inputs.get("gear_up"): btns.append("UP")
        if inputs.get("gear_down"): btns.append("DN")
        if inputs.get("select"): btns.append("SEL")
        if inputs.get("start"): btns.append("STR")
        btn_str = " ".join(btns) if btns else "-"

        usb = metrics.get("usb", {})
        usb_str = ""
        if usb.get("reverse_active"):
            usb_str = " | \033[96mUSB: ⚡TUNNEL\033[0m"
        elif usb.get("has_device"):
            usb_str = " | \033[96mUSB: ATTACHED\033[0m"

        line = (
            f"[{status_str}] "
            f"Rate: {rate:4.1f}Hz | "
            f"Steer {steer_bar} {steer:+.2f} | "
            f"Thr {throt_bar} {throttle:.2f} | "
            f"Brk {brake_bar} {brake:.2f} | "
            f"Btns: {btn_str:<10}"
            f"{usb_str}"
        )
        return line
