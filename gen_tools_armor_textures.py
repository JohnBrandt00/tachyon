from PIL import Image, ImageDraw
import os, random

BD = os.path.join("d:", os.sep, "MCMODDING", "tachyon", "src", "main", "resources", "assets", "tachyon", "textures", "item")
AD = os.path.join("d:", os.sep, "MCMODDING", "tachyon", "src", "main", "resources", "assets", "tachyon", "textures", "models", "armor")
os.makedirs(BD, exist_ok=True)
os.makedirs(AD, exist_ok=True)
random.seed(42)

# Tachyon alloy palette: purple-metallic
ALLOY_DARK = (90, 60, 130, 255)
ALLOY_MID = (130, 90, 175, 255)
ALLOY_LIGHT = (170, 130, 210, 255)
ALLOY_HIGHLIGHT = (210, 190, 240, 255)
STICK_DARK = (100, 70, 40, 255)
STICK_MID = (140, 100, 55, 255)

def add_noise(img, amount=6):
    pixels = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if a == 0: continue
            n = random.randint(-amount, amount)
            pixels[x, y] = (max(0, min(255, r+n)), max(0, min(255, g+n)), max(0, min(255, b+n)), a)

def make_pickaxe():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Head (top)
    for x in range(3, 13):
        draw.point((x, 1), fill=ALLOY_DARK)
        draw.point((x, 2), fill=ALLOY_MID)
        draw.point((x, 3), fill=ALLOY_LIGHT)
    draw.point((7, 2), fill=ALLOY_HIGHLIGHT)
    draw.point((8, 2), fill=ALLOY_HIGHLIGHT)
    # Stick
    for y in range(4, 14):
        draw.point((7, y), fill=STICK_DARK)
        draw.point((8, y), fill=STICK_MID)
    add_noise(img)
    img.save(os.path.join(BD, "tachyon_pickaxe.png"))
    print("  Created tachyon_pickaxe.png")

def make_sword():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Blade
    for y in range(1, 9):
        draw.point((7, y), fill=ALLOY_DARK)
        draw.point((8, y), fill=ALLOY_MID)
    draw.point((7, 1), fill=ALLOY_HIGHLIGHT)
    draw.point((8, 1), fill=ALLOY_LIGHT)
    # Guard
    for x in range(5, 11):
        draw.point((x, 9), fill=ALLOY_DARK)
    # Handle
    for y in range(10, 15):
        draw.point((7, y), fill=STICK_DARK)
        draw.point((8, y), fill=STICK_MID)
    add_noise(img)
    img.save(os.path.join(BD, "tachyon_sword.png"))
    print("  Created tachyon_sword.png")

def make_axe():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Axe head
    for y in range(1, 6):
        draw.point((5, y), fill=ALLOY_DARK)
        draw.point((6, y), fill=ALLOY_MID)
        draw.point((7, y), fill=ALLOY_LIGHT)
    draw.point((4, 2), fill=ALLOY_DARK)
    draw.point((4, 3), fill=ALLOY_MID)
    draw.point((4, 4), fill=ALLOY_DARK)
    draw.point((7, 2), fill=ALLOY_HIGHLIGHT)
    # Stick
    for y in range(4, 15):
        draw.point((8, y), fill=STICK_DARK)
        draw.point((9, y), fill=STICK_MID)
    add_noise(img)
    img.save(os.path.join(BD, "tachyon_axe.png"))
    print("  Created tachyon_axe.png")

def make_shovel():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Head
    draw.point((7, 1), fill=ALLOY_LIGHT)
    draw.point((6, 2), fill=ALLOY_DARK)
    draw.point((7, 2), fill=ALLOY_MID)
    draw.point((8, 2), fill=ALLOY_DARK)
    draw.point((6, 3), fill=ALLOY_MID)
    draw.point((7, 3), fill=ALLOY_HIGHLIGHT)
    draw.point((8, 3), fill=ALLOY_MID)
    draw.point((7, 4), fill=ALLOY_LIGHT)
    # Stick
    for y in range(5, 15):
        draw.point((7, y), fill=STICK_DARK)
        draw.point((8, y), fill=STICK_MID)
    add_noise(img)
    img.save(os.path.join(BD, "tachyon_shovel.png"))
    print("  Created tachyon_shovel.png")

def make_wrench():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # T-shaped wrench head
    for x in range(3, 13):
        draw.point((x, 2), fill=ALLOY_DARK)
        draw.point((x, 3), fill=ALLOY_MID)
    draw.point((3, 4), fill=ALLOY_DARK)
    draw.point((4, 4), fill=ALLOY_MID)
    draw.point((11, 4), fill=ALLOY_DARK)
    draw.point((12, 4), fill=ALLOY_MID)
    draw.point((7, 2), fill=ALLOY_HIGHLIGHT)
    draw.point((8, 2), fill=ALLOY_HIGHLIGHT)
    # Handle
    for y in range(4, 14):
        draw.point((7, y), fill=(120, 120, 130, 255))
        draw.point((8, y), fill=(150, 150, 160, 255))
    add_noise(img)
    img.save(os.path.join(BD, "tachyon_wrench.png"))
    print("  Created tachyon_wrench.png")

def make_armor_item(name, draw_fn):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw_fn(draw)
    add_noise(img)
    img.save(os.path.join(BD, f"{name}.png"))
    print(f"  Created {name}.png")

def draw_helmet(draw):
    for x in range(4, 12):
        draw.point((x, 3), fill=ALLOY_LIGHT)
    for y in range(4, 9):
        draw.point((4, y), fill=ALLOY_DARK)
        draw.point((11, y), fill=ALLOY_DARK)
    for x in range(5, 11):
        draw.point((x, 4), fill=ALLOY_MID)
        draw.point((x, 5), fill=ALLOY_HIGHLIGHT)
        draw.point((x, 6), fill=ALLOY_MID)
    for x in range(5, 11):
        draw.point((x, 8), fill=ALLOY_DARK)
    # Visor opening
    for x in range(5, 11):
        draw.point((x, 9), fill=(30, 25, 40, 255))
        draw.point((x, 10), fill=(30, 25, 40, 255))
    for y in range(9, 11):
        draw.point((4, y), fill=ALLOY_DARK)
        draw.point((11, y), fill=ALLOY_DARK)
    for x in range(4, 12):
        draw.point((x, 11), fill=ALLOY_DARK)

def draw_chestplate(draw):
    for x in range(3, 13):
        draw.point((x, 2), fill=ALLOY_LIGHT)
    for y in range(3, 13):
        draw.point((3, y), fill=ALLOY_DARK)
        draw.point((12, y), fill=ALLOY_DARK)
    for y in range(3, 13):
        for x in range(4, 12):
            draw.point((x, y), fill=ALLOY_MID)
    # Shoulders
    draw.point((2, 3), fill=ALLOY_DARK)
    draw.point((2, 4), fill=ALLOY_MID)
    draw.point((13, 3), fill=ALLOY_DARK)
    draw.point((13, 4), fill=ALLOY_MID)
    # Neck hole
    for x in range(6, 10):
        draw.point((x, 2), fill=(0, 0, 0, 0))
        draw.point((x, 3), fill=(0, 0, 0, 0))
    # Center highlight
    draw.point((7, 6), fill=ALLOY_HIGHLIGHT)
    draw.point((8, 6), fill=ALLOY_HIGHLIGHT)
    for x in range(3, 13):
        draw.point((x, 12), fill=ALLOY_DARK)

def draw_leggings(draw):
    for x in range(4, 12):
        draw.point((x, 2), fill=ALLOY_LIGHT)
    for y in range(3, 8):
        draw.point((4, y), fill=ALLOY_DARK)
        draw.point((11, y), fill=ALLOY_DARK)
        for x in range(5, 11):
            draw.point((x, y), fill=ALLOY_MID)
    # Belt
    for x in range(4, 12):
        draw.point((x, 3), fill=ALLOY_LIGHT)
    # Legs
    for y in range(8, 14):
        draw.point((4, y), fill=ALLOY_DARK)
        draw.point((5, y), fill=ALLOY_MID)
        draw.point((6, y), fill=ALLOY_MID)
        draw.point((9, y), fill=ALLOY_MID)
        draw.point((10, y), fill=ALLOY_MID)
        draw.point((11, y), fill=ALLOY_DARK)
    # Gap between legs
    for y in range(8, 14):
        draw.point((7, y), fill=(0, 0, 0, 0))
        draw.point((8, y), fill=(0, 0, 0, 0))

def draw_boots(draw):
    # Left boot
    for y in range(6, 12):
        draw.point((3, y), fill=ALLOY_DARK)
        draw.point((4, y), fill=ALLOY_MID)
        draw.point((5, y), fill=ALLOY_MID)
        draw.point((6, y), fill=ALLOY_DARK)
    draw.point((3, 12), fill=ALLOY_DARK)
    draw.point((2, 12), fill=ALLOY_MID)
    # Right boot
    for y in range(6, 12):
        draw.point((9, y), fill=ALLOY_DARK)
        draw.point((10, y), fill=ALLOY_MID)
        draw.point((11, y), fill=ALLOY_MID)
        draw.point((12, y), fill=ALLOY_DARK)
    draw.point((12, 12), fill=ALLOY_DARK)
    draw.point((13, 12), fill=ALLOY_MID)
    # Tops
    for x in range(3, 7):
        draw.point((x, 6), fill=ALLOY_LIGHT)
    for x in range(9, 13):
        draw.point((x, 6), fill=ALLOY_LIGHT)

def make_armor_layer(filename, w, h, color):
    """Create a simple solid-color armor layer texture."""
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Just fill with a semi-transparent version of the color
    for y in range(h):
        for x in range(w):
            draw.point((x, y), fill=color)
    add_noise(img, 8)
    img.save(os.path.join(AD, filename))
    print(f"  Created armor layer {filename}")

if __name__ == "__main__":
    print("Generating tool textures...")
    make_pickaxe()
    make_sword()
    make_axe()
    make_shovel()
    make_wrench()
    print("Generating armor item textures...")
    make_armor_item("tachyon_helmet", draw_helmet)
    make_armor_item("tachyon_chestplate", draw_chestplate)
    make_armor_item("tachyon_leggings", draw_leggings)
    make_armor_item("tachyon_boots", draw_boots)
    print("Generating armor layer textures...")
    # Layer 1: helmet, chestplate, boots (64x32)
    make_armor_layer("tachyon_alloy_layer_1.png", 64, 32, ALLOY_MID)
    # Layer 2: leggings (64x32)
    make_armor_layer("tachyon_alloy_layer_2.png", 64, 32, ALLOY_MID)
    print("Done!")
