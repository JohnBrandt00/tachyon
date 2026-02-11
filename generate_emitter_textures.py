"""Generate placeholder textures for the Superluminal Emitter system."""
from PIL import Image, ImageDraw
import os

TEX_DIR = os.path.join("src", "main", "resources", "assets", "tachyon", "textures")
BLOCK_DIR = os.path.join(TEX_DIR, "block")
ITEM_DIR = os.path.join(TEX_DIR, "item")
GUI_DIR = os.path.join(TEX_DIR, "gui")

os.makedirs(BLOCK_DIR, exist_ok=True)
os.makedirs(ITEM_DIR, exist_ok=True)
os.makedirs(GUI_DIR, exist_ok=True)

# Use tachyon_metal_block as base for recoloring
base_block = Image.open(os.path.join(BLOCK_DIR, "tachyon_metal_block.png")).convert("RGBA")

def recolor_block(base, hue_shift_r, hue_shift_g, hue_shift_b, brightness=1.0):
    """Recolor a block texture by applying a color tint."""
    img = base.copy()
    pixels = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            # Grayscale value
            gray = (r + g + b) / 3.0 / 255.0
            gray *= brightness
            nr = int(min(255, gray * hue_shift_r))
            ng = int(min(255, gray * hue_shift_g))
            nb = int(min(255, gray * hue_shift_b))
            pixels[x, y] = (nr, ng, nb, a)
    return img

# Superluminal Emitter (dark metallic purple/blue)
emitter = recolor_block(base_block, 120, 80, 180)
emitter.save(os.path.join(BLOCK_DIR, "superluminal_emitter.png"))

# Superluminal Emitter Active (brighter purple/cyan glow)
emitter_active = recolor_block(base_block, 160, 120, 255, brightness=1.3)
emitter_active.save(os.path.join(BLOCK_DIR, "superluminal_emitter_active.png"))

# Tachyon Light Generator (dark cyan/teal)
gen = recolor_block(base_block, 60, 140, 160)
gen.save(os.path.join(BLOCK_DIR, "tachyon_light_generator.png"))

# Tachyon Light Generator Active (bright cyan/white glow)
gen_active = recolor_block(base_block, 140, 255, 255, brightness=1.4)
gen_active.save(os.path.join(BLOCK_DIR, "tachyon_light_generator_active.png"))

# Tachyon Shard item texture (16x16, crystal shard shape)
shard = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
draw = ImageDraw.Draw(shard)

# Draw a crystal shard shape - elongated diamond
# Core shape
shard_points = [
    (8, 1),   # top
    (11, 5),  # right upper
    (10, 12), # right lower
    (8, 15),  # bottom
    (6, 12),  # left lower
    (5, 5),   # left upper
]
draw.polygon(shard_points, fill=(100, 200, 255, 255))

# Highlight on left face
highlight_points = [
    (8, 1),
    (5, 5),
    (6, 12),
    (8, 15),
]
draw.polygon(highlight_points, fill=(160, 230, 255, 255))

# Bright center line
draw.line([(8, 2), (8, 14)], fill=(220, 250, 255, 255), width=1)
draw.line([(7, 3), (7, 13)], fill=(180, 240, 255, 200), width=1)

# Outline
draw.polygon(shard_points, outline=(40, 100, 160, 255))

shard.save(os.path.join(ITEM_DIR, "tachyon_shard.png"))

# GUI texture (176x166 with 256x256 atlas)
gui = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
draw = ImageDraw.Draw(gui)

# Main background (176x166)
# Dark border
draw.rectangle([(0, 0), (175, 165)], fill=(198, 198, 198, 255))
# Inner area (lighter)
draw.rectangle([(4, 4), (171, 161)], fill=(198, 198, 198, 255))
# Top edge highlight
draw.rectangle([(0, 0), (175, 0)], fill=(255, 255, 255, 255))
draw.rectangle([(0, 0), (0, 165)], fill=(255, 255, 255, 255))
# Bottom/right shadow
draw.rectangle([(175, 0), (175, 165)], fill=(85, 85, 85, 255))
draw.rectangle([(0, 165), (175, 165)], fill=(85, 85, 85, 255))
# Second highlight line
draw.rectangle([(1, 1), (174, 1)], fill=(255, 255, 255, 255))
draw.rectangle([(1, 1), (1, 164)], fill=(255, 255, 255, 255))
# Second shadow line
draw.rectangle([(174, 1), (174, 164)], fill=(85, 85, 85, 255))
draw.rectangle([(1, 164), (174, 164)], fill=(85, 85, 85, 255))
# Inner border
draw.rectangle([(2, 2), (173, 163)], fill=(198, 198, 198, 255))
# Dark inner border for title area
draw.rectangle([(3, 3), (172, 14)], fill=(198, 198, 198, 255))

# Fuel slot background (at 80, 35 in GUI, slot is 16x16 with 1px border)
def draw_slot(draw, x, y):
    """Draw a standard inventory slot at the given position."""
    # Dark top-left edges
    draw.rectangle([(x-1, y-1), (x+16, y-1)], fill=(85, 85, 85, 255))
    draw.rectangle([(x-1, y-1), (x-1, y+16)], fill=(85, 85, 85, 255))
    # Light bottom-right edges
    draw.rectangle([(x+16, y-1), (x+16, y+16)], fill=(255, 255, 255, 255))
    draw.rectangle([(x-1, y+16), (x+16, y+16)], fill=(255, 255, 255, 255))
    # Slot interior
    draw.rectangle([(x, y), (x+15, y+15)], fill=(139, 139, 139, 255))

# Fuel slot (center)
draw_slot(draw, 80, 35)

# 3 upgrade slots (greyed out)
for i in range(3):
    draw_slot(draw, 26 + i * 27, 62)

# Player inventory slots (3 rows of 9)
for row in range(3):
    for col in range(9):
        draw_slot(draw, 8 + col * 18, 84 + row * 18)

# Hotbar slots
for col in range(9):
    draw_slot(draw, 8 + col * 18, 142)

# Flame sprite area (14x14 at position 176,0 in atlas - for the burn animation)
# Draw a simple flame
flame = Image.new("RGBA", (14, 14), (0, 0, 0, 0))
flame_draw = ImageDraw.Draw(flame)
# Orange/yellow flame
flame_draw.polygon([(7, 0), (12, 6), (10, 13), (4, 13), (2, 6)], fill=(255, 160, 30, 255))
flame_draw.polygon([(7, 2), (10, 6), (9, 11), (5, 11), (4, 6)], fill=(255, 220, 50, 255))
flame_draw.polygon([(7, 4), (8, 7), (8, 9), (6, 9), (6, 7)], fill=(255, 255, 150, 255))

gui.paste(flame, (176, 0))

gui.save(os.path.join(GUI_DIR, "superluminal_emitter.png"))

print("All textures generated successfully!")
print(f"  Block textures: {BLOCK_DIR}")
print(f"  Item textures: {ITEM_DIR}")
print(f"  GUI textures: {GUI_DIR}")
