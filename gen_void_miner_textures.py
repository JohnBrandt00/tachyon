from PIL import Image, ImageDraw
import math, random, os

BD = os.path.join("d:\\MCMODDING\\tachyon\\src\\main\\resources\\assets\\tachyon\\textures\\block")
ID = os.path.join("d:\\MCMODDING\\tachyon\\src\\main\\resources\\assets\\tachyon\\textures\\item")
GD = os.path.join("d:\\MCMODDING\\tachyon\\src\\main\\resources\\assets\\tachyon\\textures\\gui")
os.makedirs(BD, exist_ok=True)
os.makedirs(ID, exist_ok=True)
os.makedirs(GD, exist_ok=True)
random.seed(42)

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

def draw_border(draw, color, width=16, height=16):
    for x in range(width):
        draw.point((x, 0), fill=color)
        draw.point((x, height - 1), fill=color)
    for y in range(height):
        draw.point((0, y), fill=color)
        draw.point((width - 1, y), fill=color)

# ---- Void Frame Textures (4 tiers) ----

def make_void_frame():
    """Tier 1: Dark gray metallic frame with subtle grid lines"""
    img = Image.new("RGBA", (16, 16), (55, 55, 60, 255))
    draw = ImageDraw.Draw(img)
    draw_border(draw, (75, 75, 80, 255))
    # Cross-hatched frame pattern
    add_grid(img, color_offset=8, spacing=4)
    # Corner bolts
    for cx, cy in [(3, 3), (12, 3), (3, 12), (12, 12)]:
        draw.point((cx, cy), fill=(90, 90, 95, 255))
    add_noise(img, amount=5)
    img.save(os.path.join(BD, "void_frame.png"))
    print("  Created void_frame.png")

def make_stabilized_void_frame():
    """Tier 2: Darker with purple energy crystal veins"""
    img = Image.new("RGBA", (16, 16), (50, 45, 60, 255))
    draw = ImageDraw.Draw(img)
    draw_border(draw, (70, 60, 85, 255))
    add_grid(img, color_offset=6, spacing=4)
    # Purple crystal veins
    draw.point((4, 7), fill=(130, 60, 180, 255))
    draw.point((5, 8), fill=(140, 70, 190, 255))
    draw.point((7, 7), fill=(120, 50, 170, 255))
    draw.point((8, 8), fill=(130, 60, 180, 255))
    draw.point((10, 7), fill=(140, 70, 190, 255))
    draw.point((11, 8), fill=(130, 60, 180, 255))
    # Corner bolts
    for cx, cy in [(3, 3), (12, 3), (3, 12), (12, 12)]:
        draw.point((cx, cy), fill=(100, 80, 120, 255))
    add_noise(img, amount=4)
    img.save(os.path.join(BD, "stabilized_void_frame.png"))
    print("  Created stabilized_void_frame.png")

def make_reinforced_void_frame():
    """Tier 3: Dark blue-gray with cyan tachyon alloy trim"""
    img = Image.new("RGBA", (16, 16), (40, 45, 55, 255))
    draw = ImageDraw.Draw(img)
    draw_border(draw, (60, 70, 90, 255))
    add_grid(img, color_offset=5, spacing=4)
    # Cyan energy lines
    draw.line([(3, 7), (12, 7)], fill=(50, 160, 200, 255))
    draw.line([(3, 8), (12, 8)], fill=(40, 140, 180, 255))
    draw.point((7, 5), fill=(60, 180, 220, 255))
    draw.point((8, 5), fill=(60, 180, 220, 255))
    draw.point((7, 10), fill=(60, 180, 220, 255))
    draw.point((8, 10), fill=(60, 180, 220, 255))
    # Corner bolts
    for cx, cy in [(3, 3), (12, 3), (3, 12), (12, 12)]:
        draw.point((cx, cy), fill=(80, 100, 130, 255))
    add_noise(img, amount=3)
    img.save(os.path.join(BD, "reinforced_void_frame.png"))
    print("  Created reinforced_void_frame.png")

def make_quantum_void_frame():
    """Tier 4: Deep purple/black with gold quantum circuitry"""
    img = Image.new("RGBA", (16, 16), (30, 20, 40, 255))
    draw = ImageDraw.Draw(img)
    draw_border(draw, (60, 40, 80, 255))
    # Gold quantum circuit lines
    draw.line([(2, 4), (6, 4)], fill=(200, 170, 50, 255))
    draw.line([(6, 4), (6, 7)], fill=(200, 170, 50, 255))
    draw.line([(6, 7), (9, 7)], fill=(220, 190, 60, 255))
    draw.line([(9, 7), (9, 11)], fill=(200, 170, 50, 255))
    draw.line([(9, 11), (13, 11)], fill=(200, 170, 50, 255))
    # Circuit nodes
    draw.point((2, 4), fill=(255, 220, 80, 255))
    draw.point((9, 7), fill=(255, 220, 80, 255))
    draw.point((13, 11), fill=(255, 220, 80, 255))
    # Subtle purple glow spots
    draw.point((4, 10), fill=(100, 40, 160, 255))
    draw.point((11, 4), fill=(100, 40, 160, 255))
    add_noise(img, amount=3)
    img.save(os.path.join(BD, "quantum_void_frame.png"))
    print("  Created quantum_void_frame.png")

# ---- Void Miner Controller Textures ----

def make_controller():
    """Unformed controller: dark metal with central void symbol"""
    img = Image.new("RGBA", (16, 16), (45, 45, 55, 255))
    draw = ImageDraw.Draw(img)
    draw_border(draw, (70, 70, 85, 255))
    # Center void symbol (diamond/rhombus shape)
    draw.point((7, 4), fill=(80, 50, 120, 255))
    draw.point((6, 5), fill=(80, 50, 120, 255))
    draw.point((8, 5), fill=(80, 50, 120, 255))
    draw.point((5, 6), fill=(80, 50, 120, 255))
    draw.point((9, 6), fill=(80, 50, 120, 255))
    draw.point((5, 7), fill=(90, 60, 130, 255))
    draw.point((9, 7), fill=(90, 60, 130, 255))
    draw.point((5, 8), fill=(80, 50, 120, 255))
    draw.point((9, 8), fill=(80, 50, 120, 255))
    draw.point((5, 9), fill=(80, 50, 120, 255))
    draw.point((9, 9), fill=(80, 50, 120, 255))
    draw.point((6, 10), fill=(80, 50, 120, 255))
    draw.point((8, 10), fill=(80, 50, 120, 255))
    draw.point((7, 11), fill=(80, 50, 120, 255))
    # Inner void
    draw.rectangle([6, 6, 8, 9], fill=(20, 10, 30, 255))
    draw.point((7, 7), fill=(40, 20, 60, 255))
    draw.point((7, 8), fill=(40, 20, 60, 255))
    add_noise(img, amount=4)
    img.save(os.path.join(BD, "void_miner_controller.png"))
    print("  Created void_miner_controller.png")

def make_controller_formed():
    """Formed controller: purple glow around void symbol"""
    img = Image.new("RGBA", (16, 16), (45, 40, 60, 255))
    draw = ImageDraw.Draw(img)
    draw_border(draw, (80, 60, 100, 255))
    # Glowing void symbol
    draw.point((7, 4), fill=(140, 80, 200, 255))
    draw.point((6, 5), fill=(140, 80, 200, 255))
    draw.point((8, 5), fill=(140, 80, 200, 255))
    draw.point((5, 6), fill=(130, 70, 190, 255))
    draw.point((9, 6), fill=(130, 70, 190, 255))
    draw.point((5, 7), fill=(150, 90, 210, 255))
    draw.point((9, 7), fill=(150, 90, 210, 255))
    draw.point((5, 8), fill=(130, 70, 190, 255))
    draw.point((9, 8), fill=(130, 70, 190, 255))
    draw.point((5, 9), fill=(130, 70, 190, 255))
    draw.point((9, 9), fill=(130, 70, 190, 255))
    draw.point((6, 10), fill=(140, 80, 200, 255))
    draw.point((8, 10), fill=(140, 80, 200, 255))
    draw.point((7, 11), fill=(140, 80, 200, 255))
    # Inner void (deeper)
    draw.rectangle([6, 6, 8, 9], fill=(15, 5, 25, 255))
    draw.point((7, 7), fill=(60, 30, 100, 255))
    draw.point((7, 8), fill=(60, 30, 100, 255))
    add_noise(img, amount=3)
    img.save(os.path.join(BD, "void_miner_controller_formed.png"))
    print("  Created void_miner_controller_formed.png")

def make_controller_active():
    """Active controller: bright purple/white glow, void pulsing"""
    img = Image.new("RGBA", (16, 16), (50, 35, 70, 255))
    draw = ImageDraw.Draw(img)
    draw_border(draw, (100, 70, 140, 255))
    # Bright glowing void symbol
    draw.point((7, 4), fill=(200, 140, 255, 255))
    draw.point((6, 5), fill=(200, 140, 255, 255))
    draw.point((8, 5), fill=(200, 140, 255, 255))
    draw.point((5, 6), fill=(190, 130, 245, 255))
    draw.point((9, 6), fill=(190, 130, 245, 255))
    draw.point((5, 7), fill=(220, 170, 255, 255))
    draw.point((9, 7), fill=(220, 170, 255, 255))
    draw.point((5, 8), fill=(190, 130, 245, 255))
    draw.point((9, 8), fill=(190, 130, 245, 255))
    draw.point((5, 9), fill=(190, 130, 245, 255))
    draw.point((9, 9), fill=(190, 130, 245, 255))
    draw.point((6, 10), fill=(200, 140, 255, 255))
    draw.point((8, 10), fill=(200, 140, 255, 255))
    draw.point((7, 11), fill=(200, 140, 255, 255))
    # Inner void (bright core)
    draw.rectangle([6, 6, 8, 9], fill=(10, 0, 20, 255))
    draw.point((7, 7), fill=(180, 100, 255, 255))
    draw.point((7, 8), fill=(160, 80, 240, 255))
    add_noise(img, amount=3)
    img.save(os.path.join(BD, "void_miner_controller_active.png"))
    print("  Created void_miner_controller_active.png")

# ---- Void Miner Port Texture ----

def make_void_miner_port():
    """Port block: dark metal with hopper-like opening"""
    img = Image.new("RGBA", (16, 16), (55, 55, 65, 255))
    draw = ImageDraw.Draw(img)
    draw_border(draw, (80, 80, 95, 255))
    # Port opening (circular-ish)
    draw.rectangle([5, 5, 10, 10], fill=(30, 30, 40, 255))
    draw.rectangle([6, 6, 9, 9], fill=(20, 20, 30, 255))
    # Arrow indicators on edges
    draw.point((7, 3), fill=(100, 80, 140, 255))
    draw.point((8, 3), fill=(100, 80, 140, 255))
    draw.point((7, 12), fill=(100, 80, 140, 255))
    draw.point((8, 12), fill=(100, 80, 140, 255))
    draw.point((3, 7), fill=(100, 80, 140, 255))
    draw.point((3, 8), fill=(100, 80, 140, 255))
    draw.point((12, 7), fill=(100, 80, 140, 255))
    draw.point((12, 8), fill=(100, 80, 140, 255))
    add_noise(img, amount=4)
    img.save(os.path.join(BD, "void_miner_port.png"))
    print("  Created void_miner_port.png")

# ---- Module Item Textures (16x16 items) ----

def make_module_texture(name, base_color, accent_color, symbol_func):
    """Create a module item texture with a circuit board look"""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Module card shape (rounded rectangle-ish)
    draw.rectangle([3, 2, 12, 13], fill=base_color)
    draw.rectangle([4, 1, 11, 14], fill=base_color)
    # Border highlight
    draw.line([(4, 1), (11, 1)], fill=tuple(min(255, c + 30) for c in base_color[:3]) + (255,))
    draw.line([(3, 2), (3, 13)], fill=tuple(min(255, c + 30) for c in base_color[:3]) + (255,))
    draw.line([(4, 14), (11, 14)], fill=tuple(max(0, c - 30) for c in base_color[:3]) + (255,))
    draw.line([(12, 2), (12, 13)], fill=tuple(max(0, c - 30) for c in base_color[:3]) + (255,))
    # Circuit traces
    draw.line([(5, 3), (5, 5)], fill=accent_color)
    draw.line([(5, 5), (7, 5)], fill=accent_color)
    draw.line([(10, 3), (10, 5)], fill=accent_color)
    draw.line([(10, 5), (8, 5)], fill=accent_color)
    draw.line([(5, 11), (5, 12)], fill=accent_color)
    draw.line([(10, 11), (10, 12)], fill=accent_color)
    # Symbol in center
    symbol_func(draw, accent_color)
    # Contact pins at bottom
    for x in [5, 7, 9]:
        draw.point((x, 14), fill=(180, 170, 100, 255))
    add_noise(img, amount=3)
    img.save(os.path.join(ID, f"{name}.png"))
    print(f"  Created {name}.png")

def ore_symbol(draw, color):
    # Pickaxe shape
    draw.point((6, 7), fill=color)
    draw.point((7, 7), fill=color)
    draw.point((8, 7), fill=color)
    draw.point((9, 7), fill=color)
    draw.point((7, 8), fill=color)
    draw.point((8, 9), fill=color)
    draw.point((9, 10), fill=color)

def gem_symbol(draw, color):
    # Diamond shape
    draw.point((7, 6), fill=color)
    draw.point((8, 6), fill=color)
    draw.point((6, 7), fill=color)
    draw.point((9, 7), fill=color)
    draw.point((6, 8), fill=color)
    draw.point((9, 8), fill=color)
    draw.point((7, 9), fill=color)
    draw.point((8, 9), fill=color)
    draw.point((7, 10), fill=color)
    draw.point((8, 10), fill=color)

def rare_earth_symbol(draw, color):
    # RE letters
    draw.line([(6, 7), (6, 10)], fill=color)
    draw.line([(6, 7), (8, 7)], fill=color)
    draw.line([(6, 8), (7, 8)], fill=color)
    draw.point((9, 7), fill=color)
    draw.line([(9, 7), (10, 7)], fill=color)
    draw.line([(9, 8), (10, 8)], fill=color)
    draw.line([(9, 10), (10, 10)], fill=color)
    draw.point((9, 9), fill=color)

def nether_symbol(draw, color):
    # Flame shape
    draw.point((7, 10), fill=color)
    draw.point((8, 10), fill=color)
    draw.point((7, 9), fill=color)
    draw.point((8, 9), fill=color)
    draw.point((6, 8), fill=color)
    draw.point((9, 8), fill=color)
    draw.point((7, 7), fill=color)
    draw.point((8, 7), fill=color)
    draw.point((7, 6), fill=color)

def end_symbol(draw, color):
    # Ender eye shape
    draw.point((6, 8), fill=color)
    draw.point((7, 7), fill=color)
    draw.point((8, 7), fill=color)
    draw.point((9, 8), fill=color)
    draw.point((7, 9), fill=color)
    draw.point((8, 9), fill=color)
    # Pupil
    draw.point((7, 8), fill=(20, 200, 120, 255))
    draw.point((8, 8), fill=(20, 200, 120, 255))

def exotic_symbol(draw, color):
    # Star/sparkle
    draw.point((7, 6), fill=color)
    draw.point((8, 6), fill=color)
    draw.point((6, 7), fill=color)
    draw.point((9, 7), fill=color)
    draw.point((6, 8), fill=color)
    draw.point((9, 8), fill=color)
    draw.point((7, 9), fill=color)
    draw.point((8, 9), fill=color)
    draw.point((7, 7), fill=(255, 255, 200, 255))
    draw.point((8, 8), fill=(255, 255, 200, 255))

def speed_symbol(draw, color):
    # Lightning bolt
    draw.point((8, 6), fill=color)
    draw.point((7, 7), fill=color)
    draw.point((8, 7), fill=color)
    draw.point((7, 8), fill=color)
    draw.point((8, 8), fill=color)
    draw.point((9, 8), fill=color)
    draw.point((7, 9), fill=color)
    draw.point((8, 9), fill=color)
    draw.point((7, 10), fill=color)

def yield_symbol(draw, color):
    # Plus/multiply
    draw.point((7, 6), fill=color)
    draw.point((8, 6), fill=color)
    draw.point((7, 10), fill=color)
    draw.point((8, 10), fill=color)
    draw.point((6, 7), fill=color)
    draw.point((9, 7), fill=color)
    draw.point((6, 9), fill=color)
    draw.point((9, 9), fill=color)
    draw.point((7, 8), fill=color)
    draw.point((8, 8), fill=color)

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

def make_void_miner_gui():
    """
    Void Miner GUI:
    - Energy bar at (10, 16), 16x52
    - Catalyst slot at (34, 35)
    - Progress arrow at (59, 34), 24x17
    - Output slots: 3x2 grid at (92,26),(110,26),(128,26),(92,46),(110,46),(128,46)
    - Module slots: row of 6 at (8,66),(26,66),(44,66),(62,66),(80,66),(98,66)
    - Player inv at y=84, hotbar y=142

    UV extras at x=176:
    - (176, 0): energy bar fill 16x52
    - (176, 52): arrow fill 24x17
    """
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    ox, oy = 0, 0
    draw_mc_bg(draw, ox, oy)

    # Energy bar outline
    draw_energy_bar_outline(draw, ox + 10, oy + 16)

    # Catalyst slot
    draw_slot(draw, ox + 33, oy + 34)

    # Progress arrow outline
    draw_arrow_outline(draw, ox + 59, oy + 34)

    # Output slots (3x2 grid)
    for row in range(2):
        for col in range(3):
            draw_slot(draw, ox + 91 + col * 18, oy + 25 + row * 20)

    # Module slots (row of 6)
    for i in range(6):
        draw_slot(draw, ox + 7 + i * 18, oy + 65)

    # Player inventory
    draw_inv(draw, ox, oy)

    # UV extras: energy bar fill (purple gradient for void theme)
    for y in range(52):
        t = y / 51.0  # 0 = top (full), 1 = bottom (empty)
        r = int(120 + 80 * (1 - t))
        g = int(30 + 20 * t)
        b = int(180 + 75 * (1 - t))
        draw.line([(176, y), (191, y)], fill=(r, g, b, 255))

    # UV extras: arrow fill (white)
    draw_arrow_filled(draw, 176, 52)

    img.save(os.path.join(GD, "void_miner.png"))
    print("  Created void_miner.png (GUI)")


if __name__ == "__main__":
    print("Generating Void Frame block textures...")
    make_void_frame()
    make_stabilized_void_frame()
    make_reinforced_void_frame()
    make_quantum_void_frame()

    print("\nGenerating Void Miner Controller textures...")
    make_controller()
    make_controller_formed()
    make_controller_active()

    print("\nGenerating Void Miner Port texture...")
    make_void_miner_port()

    print("\nGenerating module item textures...")
    make_module_texture("ore_extraction_module", (80, 80, 90, 255), (180, 140, 60, 255), ore_symbol)
    make_module_texture("gem_resonance_module", (70, 70, 95, 255), (100, 200, 255, 255), gem_symbol)
    make_module_texture("rare_earth_module", (75, 80, 75, 255), (60, 180, 100, 255), rare_earth_symbol)
    make_module_texture("nether_siphon_module", (90, 60, 55, 255), (255, 140, 40, 255), nether_symbol)
    make_module_texture("end_siphon_module", (60, 60, 80, 255), (180, 100, 255, 255), end_symbol)
    make_module_texture("exotic_attunement_module", (50, 30, 60, 255), (255, 200, 100, 255), exotic_symbol)
    make_module_texture("speed_module", (80, 85, 80, 255), (255, 255, 100, 255), speed_symbol)
    make_module_texture("yield_module", (75, 85, 75, 255), (100, 255, 100, 255), yield_symbol)

    print("\nGenerating Void Miner GUI texture...")
    make_void_miner_gui()

    print("\nAll Void Miner textures generated!")
