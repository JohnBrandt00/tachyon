
from PIL import Image, ImageDraw
import os

BASE = os.path.join('d:', os.sep, 'MCMODDING', 'tachyon', 'src', 'main', 'resources', 'assets', 'tachyon', 'textures')
BLOCK = os.path.join(BASE, 'block')
GUI = os.path.join(BASE, 'gui')

os.makedirs(BLOCK, exist_ok=True)
os.makedirs(GUI, exist_ok=True)