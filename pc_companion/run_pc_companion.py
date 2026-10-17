#!/usr/bin/env python3
"""
ApexWheel PC Companion Launcher
Runs the local network server and virtual gamepad engine.
"""

import sys
import os

# Ensure src is importable
current_dir = os.path.dirname(os.path.abspath(__file__))
if current_dir not in sys.path:
    sys.path.insert(0, current_dir)

from src.app import main

if __name__ == "__main__":
    main()
