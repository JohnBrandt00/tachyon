from PIL import Image, ImageDraw
import math, random, os

BD = os.path.join("d:\\MCMODDING\\tachyon\\src\\main\\resources\\assets\\tachyon\\textures\\block")
GD = os.path.join("d:\\MCMODDING\\tachyon\\src\\main\\resources\\assets\\tachyon\\textures\\gui")
os.makedirs(BD, exist_ok=True)
os.makedirs(GD, exist_ok=True)
random.seed(77)

# Condenser palette: deep purple-blue metallic
COND_DARK = (50, 35, 75, 255)
COND_MID = (70, 50, 105, 255)
COND_LIGHT = (95, 70, 140, 255)
COND_HIGHLIGHT = (140, 110, 185, 255)
COND_ACCENT = (180, 100, 220, 255)  # Bright purple accent
COND_GLOW = (200, 140, 255, 255)    # Active glow

def add_noise(img, amount=6):
    pixels = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if a == 0: continue
            n = random.randint(-amount, amount)
            pixels[x, y] = (max(0, min(255, r+n)), max(0, min(255, g+n)), max(0, min(255, b+n)), a)

# ---- Block Textures ----

def make_condenser_casing():
    img = Image.new("RGBA", (16, 16), COND_MID)
    draw = ImageDraw.Draw(img)
    # Border
    for x in range(16):
        draw.point((x, 0), fill=COND_DARK)
        draw.point((x, 15), fill=COND_DARK)
    for y in range(16):
        draw.point((0, y), fill=COND_DARK)
        draw.point((15, y), fill=COND_DARK)
    # Inner panel lines
    draw.line([(1, 1), (14, 1)], fill=COND_LIGHT)
    draw.line([(1, 1), (1, 14)], fill=COND_LIGHT)
    # Corner rivets
    draw.point((3, 3), fill=COND_HIGHLIGHT)
    draw.point((12, 3), fill=COND_HIGHLIGHT)
    draw.point((3, 12), fill=COND_HIGHLIGHT)
    draw.point((12, 12), fill=COND_HIGHLIGHT)
    add_noise(img, amount=5)
    img.save(os.path.join(BD, "condenser_casing.png"))
    print("  Created condenser_casing.png")

def make_condenser_controller_front():
    img = Image.new("RGBA", (16, 16), COND_MID)
    draw = ImageDraw.Draw(img)
    # Border
    for x in range(16):
        draw.point((x, 0), fill=COND_DARK)
        draw.point((x, 15), fill=COND_DARK)
    for y in range(16):
        draw.point((0, y), fill=COND_DARK)
        draw.point((15, y), fill=COND_DARK)
    # Center display (dark viewport)
    draw.rectangle([4, 4, 11, 11], fill=(25, 18, 35, 255))
    # Display inner frame
    draw.rectangle([5, 5, 10, 10], fill=(35, 25, 50, 255))
    # Central indicator (inactive)
    draw.point((7, 7), fill=COND_LIGHT)
    draw.point((8, 7), fill=COND_LIGHT)
    draw.point((7, 8), fill=COND_LIGHT)
    draw.point((8, 8), fill=COND_LIGHT)
    add_noise(img, amount=4)
    img.save(os.path.join(BD, "condenser_controller_front.png"))
    print("  Created condenser_controller_front.png")

def make_condenser_controller_front_formed():
    img = Image.new("RGBA", (16, 16), COND_MID)
    draw = ImageDraw.Draw(img)
    # Border
    for x in range(16):
        draw.point((x, 0), fill=COND_DARK)
        draw.point((x, 15), fill=COND_DARK)
    for y in range(16):
        draw.point((0, y), fill=COND_DARK)
        draw.point((15, y), fill=COND_DARK)
    # Center display (active glow)
    draw.rectangle([4, 4, 11, 11], fill=(40, 25, 60, 255))
    draw.rectangle([5, 5, 10, 10], fill=(60, 35, 90, 255))
    # Glowing central indicator
    draw.point((7, 7), fill=COND_GLOW)
    draw.point((8, 7), fill=COND_GLOW)
    draw.point((7, 8), fill=COND_ACCENT)
    draw.point((8, 8), fill=COND_ACCENT)
    # Corner glow accents
    draw.point((5, 5), fill=COND_ACCENT)
    draw.point((10, 5), fill=COND_ACCENT)
    draw.point((5, 10), fill=COND_ACCENT)
    draw.point((10, 10), fill=COND_ACCENT)
    add_noise(img, amount=3)
    img.save(os.path.join(BD, "condenser_controller_front_formed.png"))
    print("  Created condenser_controller_front_formed.png")

def make_condenser_controller_top():
    img = Image.new("RGBA", (16, 16), COND_MID)
    draw = ImageDraw.Draw(img)
    # Border
    for x in range(16):
        draw.point((x, 0), fill=COND_DARK)
        draw.point((x, 15), fill=COND_DARK)
    for y in range(16):
        draw.point((0, y), fill=COND_DARK)
        draw.point((15, y), fill=COND_DARK)
    # Center vent/connector
    draw.rectangle([5, 5, 10, 10], fill=COND_DARK)
    draw.rectangle([6, 6, 9, 9], fill=COND_LIGHT)
    draw.point((7, 7), fill=COND_HIGHLIGHT)
    draw.point((8, 8), fill=COND_HIGHLIGHT)
    add_noise(img, amount=5)
    img.save(os.path.join(BD, "condenser_controller_top.png"))
    print("  Created condenser_controller_top.png")

def make_condenser_port(mode_name, accent_color):
    img = Image.new("RGBA", (16, 16), COND_MID)
    draw = ImageDraw.Draw(img)
    # Border
    for x in range(16):
        draw.point((x, 0), fill=COND_DARK)
        draw.point((x, 15), fill=COND_DARK)
    for y in range(16):
        draw.point((0, y), fill=COND_DARK)
        draw.point((15, y), fill=COND_DARK)
    # Port opening
    draw.rectangle([4, 4, 11, 11], fill=(30, 20, 45, 255))
    # Mode indicator ring
    for x in range(5, 11):
        draw.point((x, 5), fill=accent_color)
        draw.point((x, 10), fill=accent_color)
    for y in range(5, 11):
        draw.point((5, y), fill=accent_color)
        draw.point((10, y), fill=accent_color)
    # Center dot
    draw.point((7, 7), fill=accent_color)
    draw.point((8, 8), fill=accent_color)
    add_noise(img, amount=4)
    img.save(os.path.join(BD, f"condenser_port_{mode_name}.png"))
    print(f"  Created condenser_port_{mode_name}.png")

# ---- GUI Texture ----

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

def draw_fluid_tank_outline(draw, x, y, w=16, h=52):
    draw.rectangle([x, y, x+w-1, y+h-1], fill=(30, 20, 40, 255))
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

def make_condenser_gui():
    """
    Condenser Controller GUI layout:
    - Energy bar at (10, 16), 16x52
    - Fluid tank at (150, 16), 16x52
    - Input slot at (56, 35) -> slot drawn at (55, 34)
    - Arrow at (79, 34), 24x17
    - 3 upgrade slots at (62, 62), (80, 62), (98, 62) -> drawn at (61, 61), (79, 61), (97, 61)
    - Player inv at y=84, hotbar at y=142

    UV extras at x=176:
    - (176, 0) to (176+16, 52): energy bar fill (16x52)
    - (176, 52): arrow fill (24x17)
    - (176, 69) to (176+16, 121): fluid tank fill (16x52)
    """
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    ox, oy = 0, 0
    draw_mc_bg(draw, ox, oy)

    # Energy bar outline
    draw_energy_bar_outline(draw, ox+10, oy+16)

    # Fluid tank outline
    draw_fluid_tank_outline(draw, ox+150, oy+16)

    # Input slot
    draw_slot(draw, ox+55, oy+34)

    # Arrow outline
    draw_arrow_outline(draw, ox+79, oy+34)

    # Upgrade slots
    for i in range(3):
        draw_slot(draw, ox+61 + i*18, oy+61)

    # Player inventory
    draw_inv(draw, ox, oy)

    # UV extras: energy bar fill (red gradient)
    for y in range(52):
        t = y / 51.0
        r = int(200 + 55 * (1 - t))
        g = int(30 + 20 * t)
        b = int(30 + 20 * t)
        draw.line([(176, y), (191, y)], fill=(r, g, b, 255))

    # UV extras: arrow fill (white)
    draw_arrow_filled(draw, 176, 52)

    # UV extras: fluid tank fill (purple gradient)
    for y in range(52):
        t = y / 51.0
        r = int(120 + 60 * (1 - t))
        g = int(50 + 30 * (1 - t))
        b = int(180 + 40 * (1 - t))
        draw.line([(176, 69 + y), (191, 69 + y)], fill=(r, g, b, 255))

    img.save(os.path.join(GD, "condenser_controller.png"))
    print("  Created condenser_controller.png (GUI)")

if __name__ == "__main__":
    print("Generating Condenser block textures...")
    make_condenser_casing()
    make_condenser_controller_front()
    make_condenser_controller_front_formed()
    make_condenser_controller_top()
    print()
    print("Generating Condenser port textures...")
    # item_input: orange/amber for items
    make_condenser_port("item_input", (220, 170, 50, 255))
    # fluid_output: blue for fluid
    make_condenser_port("fluid_output", (60, 140, 220, 255))
    # energy_input: red for energy
    make_condenser_port("energy_input", (220, 60, 50, 255))
    print()
    print("Generating Condenser GUI texture...")
    make_condenser_gui()
    print()
    print("All condenser textures generated!")
