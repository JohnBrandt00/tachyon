from PIL import Image, ImageDraw
import math, random, os

BD = os.path.join("d:\\MCMODDING\\tachyon\\src\\main\\resources\\assets\\tachyon\\textures\\block")
GD = os.path.join("d:\\MCMODDING\\tachyon\\src\\main\\resources\\assets\\tachyon\\textures\\gui")
os.makedirs(BD, exist_ok=True)
os.makedirs(GD, exist_ok=True)
random.seed(99)

def add_noise(img, amount=8):
    pixels = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            n = random.randint(-amount, amount)
            pixels[x, y] = (max(0, min(255, r + n)), max(0, min(255, g + n)), max(0, min(255, b + n)), a)

def add_grid(img, color_offset=10, spacing=4):
    pixels = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            if x % spacing == 0 or y % spacing == 0:
                r, g, b, a = pixels[x, y]
                pixels[x, y] = (max(0, min(255, r + color_offset)), max(0, min(255, g + color_offset)), max(0, min(255, b + color_offset)), a)

# ---- Helper functions for GUI ----
def draw_slot(draw, x, y, w=18, h=18):
    draw.rectangle([x, y, x+w-1, y+h-1], fill=(139, 139, 139, 255))
    draw.line([(x, y), (x+w-1, y)], fill=(55, 55, 55, 255))
    draw.line([(x, y), (x, y+h-1)], fill=(55, 55, 55, 255))
    draw.line([(x, y+h-1), (x+w-1, y+h-1)], fill=(255, 255, 255, 255))
    draw.line([(x+w-1, y), (x+w-1, y+h-1)], fill=(255, 255, 255, 255))
    draw.rectangle([x+1, y+1, x+w-2, y+h-2], fill=(139, 139, 139, 255))

def draw_mc_bg(draw, ox, oy, w=176, h=166):
    draw.rectangle([ox, oy, ox+w-1, oy+h-1], fill=(198, 198, 198, 255))
    draw.line([(ox, oy), (ox+w-1, oy)], fill=(255, 255, 255, 255))
    draw.line([(ox, oy), (ox, oy+h-1)], fill=(255, 255, 255, 255))
    draw.line([(ox+1, oy+1), (ox+w-2, oy+1)], fill=(255, 255, 255, 255))
    draw.line([(ox+1, oy+1), (ox+1, oy+h-2)], fill=(255, 255, 255, 255))
    draw.line([(ox, oy+h-1), (ox+w-1, oy+h-1)], fill=(55, 55, 55, 255))
    draw.line([(ox+w-1, oy), (ox+w-1, oy+h-1)], fill=(55, 55, 55, 255))
    draw.line([(ox+1, oy+h-2), (ox+w-2, oy+h-2)], fill=(85, 85, 85, 255))
    draw.line([(ox+w-2, oy+1), (ox+w-2, oy+h-2)], fill=(85, 85, 85, 255))
    draw.rectangle([ox+3, oy+3, ox+w-4, oy+h-4], fill=(198, 198, 198, 255))

def draw_inv(draw, ox, oy):
    for row in range(3):
        for col in range(9):
            draw_slot(draw, ox+8+col*18, oy+84+row*18)
    for col in range(9):
        draw_slot(draw, ox+8+col*18, oy+142)

def draw_energy_bar_outline(draw, x, y, w=16, h=52):
    draw.rectangle([x, y, x+w-1, y+h-1], fill=(40, 40, 40, 255))
    draw.line([(x, y), (x+w-1, y)], fill=(55, 55, 55, 255))
    draw.line([(x, y), (x, y+h-1)], fill=(55, 55, 55, 255))
    draw.line([(x, y+h-1), (x+w-1, y+h-1)], fill=(255, 255, 255, 255))
    draw.line([(x+w-1, y), (x+w-1, y+h-1)], fill=(255, 255, 255, 255))

def draw_arrow_outline(draw, x, y, w=24, h=17, color=(160, 160, 160, 255)):
    mid_y = y + h // 2
    draw.rectangle([x, y+4, x+15, y+h-5], outline=color)
    draw.line([(x+14, y+1), (x+w-1, mid_y)], fill=color)
    draw.line([(x+14, y+h-2), (x+w-1, mid_y)], fill=color)
    draw.line([(x+14, y+1), (x+14, y+h-2)], fill=color)

def draw_arrow_filled(draw, x, y, w=24, h=17, color=(255, 255, 255, 255)):
    mid_y = y + h // 2
    draw.rectangle([x, y+4, x+15, y+h-5], fill=color)
    for row in range(h):
        py = y + row
        dist = abs(mid_y - py)
        max_dist = h // 2
        if dist <= max_dist and max_dist > 0:
            x_start = 14 + x
            x_end = int(x + w - 1 - (w - 15) * dist / max_dist)
            if x_end >= x_start:
                draw.line([(x_start, py), (x_end, py)], fill=color)

# ---- Ore Crusher Block Textures (industrial gray/iron look) ----
def make_ore_crusher_front():
    img = Image.new("RGBA", (16, 16), (100, 100, 105, 255))
    draw = ImageDraw.Draw(img)
    # Border
    bc = (120, 120, 125, 255)
    for x in range(16):
        draw.point((x, 0), fill=bc)
        draw.point((x, 15), fill=bc)
    for y in range(16):
        draw.point((0, y), fill=bc)
        draw.point((15, y), fill=bc)
    # Crusher jaw opening (dark rectangle in center)
    draw.rectangle([4, 4, 11, 11], fill=(35, 35, 40, 255))
    # Jaw teeth (v-pattern)
    draw.point((5, 5), fill=(70, 70, 75, 255))
    draw.point((7, 5), fill=(70, 70, 75, 255))
    draw.point((9, 5), fill=(70, 70, 75, 255))
    draw.point((6, 10), fill=(70, 70, 75, 255))
    draw.point((8, 10), fill=(70, 70, 75, 255))
    draw.point((10, 10), fill=(70, 70, 75, 255))
    add_noise(img, amount=4)
    img.save(os.path.join(BD, "ore_crusher_front.png"))
    print("  Created ore_crusher_front.png")

def make_ore_crusher_front_active():
    img = Image.new("RGBA", (16, 16), (100, 100, 105, 255))
    draw = ImageDraw.Draw(img)
    bc = (120, 120, 125, 255)
    for x in range(16):
        draw.point((x, 0), fill=bc)
        draw.point((x, 15), fill=bc)
    for y in range(16):
        draw.point((0, y), fill=bc)
        draw.point((15, y), fill=bc)
    # Active jaw with orange glow
    draw.rectangle([4, 4, 11, 11], fill=(45, 30, 20, 255))
    # Glowing teeth
    draw.point((5, 5), fill=(200, 140, 50, 255))
    draw.point((7, 5), fill=(220, 160, 60, 255))
    draw.point((9, 5), fill=(200, 140, 50, 255))
    draw.point((6, 10), fill=(200, 140, 50, 255))
    draw.point((8, 10), fill=(220, 160, 60, 255))
    draw.point((10, 10), fill=(200, 140, 50, 255))
    # Center glow
    draw.point((7, 7), fill=(255, 200, 80, 255))
    draw.point((8, 7), fill=(255, 200, 80, 255))
    draw.point((7, 8), fill=(240, 180, 60, 255))
    draw.point((8, 8), fill=(240, 180, 60, 255))
    add_noise(img, amount=3)
    img.save(os.path.join(BD, "ore_crusher_front_active.png"))
    print("  Created ore_crusher_front_active.png")

def make_ore_crusher_side():
    img = Image.new("RGBA", (16, 16), (100, 100, 105, 255))
    draw = ImageDraw.Draw(img)
    bc = (120, 120, 125, 255)
    for x in range(16):
        draw.point((x, 0), fill=bc)
        draw.point((x, 15), fill=bc)
    for y in range(16):
        draw.point((0, y), fill=bc)
        draw.point((15, y), fill=bc)
    # Bolts/rivets
    draw.point((3, 3), fill=(80, 80, 85, 255))
    draw.point((12, 3), fill=(80, 80, 85, 255))
    draw.point((3, 12), fill=(80, 80, 85, 255))
    draw.point((12, 12), fill=(80, 80, 85, 255))
    add_noise(img, amount=4)
    img.save(os.path.join(BD, "ore_crusher_side.png"))
    print("  Created ore_crusher_side.png")

def make_ore_crusher_top():
    img = Image.new("RGBA", (16, 16), (95, 95, 100, 255))
    draw = ImageDraw.Draw(img)
    # Input hopper opening
    draw.rectangle([5, 5, 10, 10], fill=(50, 50, 55, 255))
    draw.rectangle([6, 6, 9, 9], fill=(30, 30, 35, 255))
    add_noise(img, amount=4)
    img.save(os.path.join(BD, "ore_crusher_top.png"))
    print("  Created ore_crusher_top.png")

# ---- Alloy Forge Block Textures (warm/hot metallic look) ----
def make_alloy_forge_front():
    img = Image.new("RGBA", (16, 16), (80, 60, 50, 255))
    draw = ImageDraw.Draw(img)
    bc = (100, 75, 60, 255)
    for x in range(16):
        draw.point((x, 0), fill=bc)
        draw.point((x, 15), fill=bc)
    for y in range(16):
        draw.point((0, y), fill=bc)
        draw.point((15, y), fill=bc)
    # Forge opening
    draw.rectangle([4, 5, 11, 12], fill=(30, 20, 18, 255))
    # Grate bars
    draw.line([(6, 5), (6, 12)], fill=(60, 45, 35, 255))
    draw.line([(9, 5), (9, 12)], fill=(60, 45, 35, 255))
    add_noise(img, amount=4)
    img.save(os.path.join(BD, "alloy_forge_front.png"))
    print("  Created alloy_forge_front.png")

def make_alloy_forge_front_active():
    img = Image.new("RGBA", (16, 16), (80, 60, 50, 255))
    draw = ImageDraw.Draw(img)
    bc = (100, 75, 60, 255)
    for x in range(16):
        draw.point((x, 0), fill=bc)
        draw.point((x, 15), fill=bc)
    for y in range(16):
        draw.point((0, y), fill=bc)
        draw.point((15, y), fill=bc)
    # Glowing forge opening
    draw.rectangle([4, 5, 11, 12], fill=(180, 80, 20, 255))
    # Bright center
    draw.rectangle([5, 6, 10, 11], fill=(255, 160, 40, 255))
    draw.rectangle([6, 7, 9, 10], fill=(255, 220, 100, 255))
    # Grate bars (dark against glow)
    draw.line([(6, 5), (6, 12)], fill=(120, 50, 20, 255))
    draw.line([(9, 5), (9, 12)], fill=(120, 50, 20, 255))
    add_noise(img, amount=3)
    img.save(os.path.join(BD, "alloy_forge_front_active.png"))
    print("  Created alloy_forge_front_active.png")

def make_alloy_forge_side():
    img = Image.new("RGBA", (16, 16), (80, 60, 50, 255))
    draw = ImageDraw.Draw(img)
    bc = (100, 75, 60, 255)
    for x in range(16):
        draw.point((x, 0), fill=bc)
        draw.point((x, 15), fill=bc)
    for y in range(16):
        draw.point((0, y), fill=bc)
        draw.point((15, y), fill=bc)
    # Metal band
    draw.line([(1, 7), (14, 7)], fill=(90, 68, 55, 255))
    draw.line([(1, 8), (14, 8)], fill=(90, 68, 55, 255))
    add_noise(img, amount=4)
    img.save(os.path.join(BD, "alloy_forge_side.png"))
    print("  Created alloy_forge_side.png")

def make_alloy_forge_top():
    img = Image.new("RGBA", (16, 16), (75, 55, 45, 255))
    draw = ImageDraw.Draw(img)
    # Two input ports
    draw.rectangle([3, 5, 6, 10], fill=(40, 25, 20, 255))
    draw.rectangle([9, 5, 12, 10], fill=(40, 25, 20, 255))
    add_noise(img, amount=4)
    img.save(os.path.join(BD, "alloy_forge_top.png"))
    print("  Created alloy_forge_top.png")

# ---- GUI Textures ----
def make_ore_crusher_gui():
    """
    Ore Crusher GUI layout:
    - Energy bar at (10, 16), 16x52
    - Input slot at (56, 35) -> slot drawn at (55, 34) to account for -1 offset
    - Output slot at (116, 35) -> slot drawn at (115, 34)
    - Arrow at (79, 34), 24x17
    - 3 upgrade slots at (62, 62), (80, 62), (98, 62) -> drawn at (61, 61), (79, 61), (97, 61)
    - Player inv at y=84, hotbar at y=142

    UV extras at x=176:
    - (176, 0) to (176+16, 52): energy bar fill (16x52)
    - (176, 52): arrow fill (24x17)
    """
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    ox, oy = 0, 0
    draw_mc_bg(draw, ox, oy)

    # Energy bar outline
    draw_energy_bar_outline(draw, ox+10, oy+16)

    # Input slot
    draw_slot(draw, ox+55, oy+34)

    # Output slot
    draw_slot(draw, ox+115, oy+34)

    # Arrow outline
    draw_arrow_outline(draw, ox+79, oy+34)

    # Upgrade slots
    for i in range(3):
        draw_slot(draw, ox+61 + i*18, oy+61)

    # Player inventory
    draw_inv(draw, ox, oy)

    # UV extras: energy bar fill (red gradient)
    for y in range(52):
        t = y / 51.0  # 0 = top (full), 1 = bottom (empty)
        r = int(200 + 55 * (1 - t))
        g = int(30 + 20 * t)
        b = int(30 + 20 * t)
        draw.line([(176, y), (191, y)], fill=(r, g, b, 255))

    # UV extras: arrow fill (white)
    draw_arrow_filled(draw, 176, 52)

    img.save(os.path.join(GD, "ore_crusher.png"))
    print("  Created ore_crusher.png (GUI)")

def make_alloy_forge_gui():
    """
    Alloy Forge GUI layout:
    - Energy bar at (10, 16), 16x52
    - Input 1 slot at (52, 27) -> slot drawn at (51, 26)
    - Input 2 slot at (52, 47) -> slot drawn at (51, 46)
    - Output slot at (116, 35) -> slot drawn at (115, 34)
    - Arrow at (79, 34), 24x17
    - 3 upgrade slots at (62, 62), (80, 62), (98, 62) -> drawn at (61, 61), (79, 61), (97, 61)
    - Player inv at y=84, hotbar at y=142

    UV extras same as ore crusher
    """
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    ox, oy = 0, 0
    draw_mc_bg(draw, ox, oy)

    # Energy bar outline
    draw_energy_bar_outline(draw, ox+10, oy+16)

    # Input 1 slot
    draw_slot(draw, ox+51, oy+26)

    # Input 2 slot
    draw_slot(draw, ox+51, oy+46)

    # Output slot
    draw_slot(draw, ox+115, oy+34)

    # Arrow outline
    draw_arrow_outline(draw, ox+79, oy+34)

    # Upgrade slots
    for i in range(3):
        draw_slot(draw, ox+61 + i*18, oy+61)

    # Player inventory
    draw_inv(draw, ox, oy)

    # UV extras: energy bar fill (orange-red gradient for forge)
    for y in range(52):
        t = y / 51.0
        r = int(220 + 35 * (1 - t))
        g = int(100 + 60 * (1 - t))
        b = int(20 + 10 * t)
        draw.line([(176, y), (191, y)], fill=(r, g, b, 255))

    # UV extras: arrow fill (white)
    draw_arrow_filled(draw, 176, 52)

    img.save(os.path.join(GD, "alloy_forge.png"))
    print("  Created alloy_forge.png (GUI)")

if __name__ == "__main__":
    print("Generating Ore Crusher block textures...")
    make_ore_crusher_front()
    make_ore_crusher_front_active()
    make_ore_crusher_side()
    make_ore_crusher_top()
    print()
    print("Generating Alloy Forge block textures...")
    make_alloy_forge_front()
    make_alloy_forge_front_active()
    make_alloy_forge_side()
    make_alloy_forge_top()
    print()
    print("Generating GUI textures...")
    make_ore_crusher_gui()
    make_alloy_forge_gui()
    print()
    print("All machine textures generated!")
