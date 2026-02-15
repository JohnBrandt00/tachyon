from PIL import Image, ImageDraw
import math, random, os

BD = os.path.join(chr(100)+chr(58), os.sep, chr(77)+chr(67)+chr(77)+chr(79)+chr(68)+chr(68)+chr(73)+chr(78)+chr(71), chr(116)+chr(97)+chr(99)+chr(104)+chr(121)+chr(111)+chr(110), chr(115)+chr(114)+chr(99), chr(109)+chr(97)+chr(105)+chr(110), chr(114)+chr(101)+chr(115)+chr(111)+chr(117)+chr(114)+chr(99)+chr(101)+chr(115), chr(97)+chr(115)+chr(115)+chr(101)+chr(116)+chr(115), chr(116)+chr(97)+chr(99)+chr(104)+chr(121)+chr(111)+chr(110), chr(116)+chr(101)+chr(120)+chr(116)+chr(117)+chr(114)+chr(101)+chr(115), chr(98)+chr(108)+chr(111)+chr(99)+chr(107))
GD = os.path.join(chr(100)+chr(58), os.sep, chr(77)+chr(67)+chr(77)+chr(79)+chr(68)+chr(68)+chr(73)+chr(78)+chr(71), chr(116)+chr(97)+chr(99)+chr(104)+chr(121)+chr(111)+chr(110), chr(115)+chr(114)+chr(99), chr(109)+chr(97)+chr(105)+chr(110), chr(114)+chr(101)+chr(115)+chr(111)+chr(117)+chr(114)+chr(99)+chr(101)+chr(115), chr(97)+chr(115)+chr(115)+chr(101)+chr(116)+chr(115), chr(116)+chr(97)+chr(99)+chr(104)+chr(121)+chr(111)+chr(110), chr(116)+chr(101)+chr(120)+chr(116)+chr(117)+chr(114)+chr(101)+chr(115), chr(103)+chr(117)+chr(105))
os.makedirs(BD, exist_ok=True)
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

def make_singularity_casing():
    img = Image.new("RGBA", (16, 16), (40, 35, 50, 255))
    draw = ImageDraw.Draw(img)
    bc = (55, 48, 65, 255)
    for x in range(16):
        draw.point((x, 0), fill=bc)
        draw.point((x, 15), fill=bc)
    for y in range(16):
        draw.point((0, y), fill=bc)
        draw.point((15, y), fill=bc)
    add_grid(img, color_offset=6, spacing=4)
    add_noise(img, amount=5)
    img.save(os.path.join(BD, "singularity_casing.png"))
    print("  Created singularity_casing.png")

def make_singularity_controller_front():
    img = Image.new("RGBA", (16, 16), (40, 35, 50, 255))
    draw = ImageDraw.Draw(img)
    bc = (55, 48, 65, 255)
    for x in range(16):
        draw.point((x, 0), fill=bc)
        draw.point((x, 15), fill=bc)
    for y in range(16):
        draw.point((0, y), fill=bc)
        draw.point((15, y), fill=bc)
    draw.rectangle([5, 5, 10, 10], fill=(20, 18, 28, 255))
    draw.rectangle([7, 7, 8, 8], fill=(180, 30, 30, 255))
    add_noise(img, amount=4)
    img.save(os.path.join(BD, "singularity_controller_front.png"))
    print("  Created singularity_controller_front.png")

def make_singularity_controller_front_formed():
    img = Image.new("RGBA", (16, 16), (40, 35, 50, 255))
    draw = ImageDraw.Draw(img)
    bc = (55, 48, 65, 255)
    for x in range(16):
        draw.point((x, 0), fill=bc)
        draw.point((x, 15), fill=bc)
    for y in range(16):
        draw.point((0, y), fill=bc)
        draw.point((15, y), fill=bc)
    draw.rectangle([4, 4, 11, 11], fill=(30, 80, 100, 255))
    draw.rectangle([5, 5, 10, 10], fill=(20, 18, 28, 255))
    draw.rectangle([7, 7, 8, 8], fill=(50, 220, 255, 255))
    add_noise(img, amount=4)
    img.save(os.path.join(BD, "singularity_controller_front_formed.png"))
    print("  Created singularity_controller_front_formed.png")

def make_singularity_port():
    img = Image.new("RGBA", (16, 16), (40, 35, 50, 255))
    draw = ImageDraw.Draw(img)
    bc = (55, 48, 65, 255)
    for x in range(16):
        draw.point((x, 0), fill=bc)
        draw.point((x, 15), fill=bc)
    for y in range(16):
        draw.point((0, y), fill=bc)
        draw.point((15, y), fill=bc)
    hc = (25, 22, 35, 255)
    for i in range(4):
        for j in range(4):
            draw.point((5 + i * 2, 5 + j * 2), fill=hc)
    add_noise(img, amount=4)
    img.save(os.path.join(BD, "singularity_port.png"))
    print("  Created singularity_port.png")

def make_exotic_matter_core():
    img = Image.new("RGBA", (16, 16), (50, 20, 70, 255))
    pixels = img.load()
    cx, cy = 7.5, 7.5
    bright = (200, 50, 220)
    base = (50, 20, 70)
    for y in range(16):
        for x in range(16):
            dist = math.sqrt((x - cx) ** 2 + (y - cy) ** 2)
            if dist < 2.5:
                t = dist / 2.5
                r = int(bright[0] + (180 - bright[0]) * (1 - t))
                g = int(bright[1] + (120 - bright[1]) * (1 - t))
                b = int(bright[2] + (255 - bright[2]) * (1 - t))
                pixels[x, y] = (min(255, r), min(255, g), min(255, b), 255)
            elif dist < 8.0:
                t = (dist - 2.5) / 5.5
                r = int(bright[0] * (1 - t) + base[0] * t)
                g = int(bright[1] * (1 - t) + base[1] * t)
                b = int(bright[2] * (1 - t) + base[2] * t)
                pixels[x, y] = (r, g, b, 255)
    add_noise(img, amount=6)
    img.save(os.path.join(BD, "exotic_matter_core.png"))
    print("  Created exotic_matter_core.png")

def make_photonic_injector_front():
    img = Image.new("RGBA", (16, 16), (60, 60, 65, 255))
    pixels = img.load()
    draw = ImageDraw.Draw(img)
    bc = (75, 75, 80, 255)
    for x in range(16):
        draw.point((x, 0), fill=bc)
        draw.point((x, 15), fill=bc)
    for y in range(16):
        draw.point((0, y), fill=bc)
        draw.point((15, y), fill=bc)
    lc = (30, 30, 35, 255)
    for y in range(16):
        for x in range(16):
            if math.sqrt((x - 7.5) ** 2 + (y - 7.5) ** 2) <= 4.0:
                pixels[x, y] = lc
    add_noise(img, amount=3)
    img.save(os.path.join(BD, "photonic_injector_front.png"))
    print("  Created photonic_injector_front.png")

def make_photonic_injector_front_active():
    img = Image.new("RGBA", (16, 16), (60, 60, 65, 255))
    pixels = img.load()
    draw = ImageDraw.Draw(img)
    bc = (75, 75, 80, 255)
    for x in range(16):
        draw.point((x, 0), fill=bc)
        draw.point((x, 15), fill=bc)
    for y in range(16):
        draw.point((0, y), fill=bc)
        draw.point((15, y), fill=bc)
    lc = (200, 230, 255, 255)
    gi = (150, 200, 240, 255)
    for y in range(16):
        for x in range(16):
            d = math.sqrt((x - 7.5) ** 2 + (y - 7.5) ** 2)
            if d <= 2.0:
                pixels[x, y] = lc
            elif d <= 4.0:
                t = (d - 2.0) / 2.0
                pixels[x, y] = (int(lc[0]*(1-t)+gi[0]*t), int(lc[1]*(1-t)+gi[1]*t), int(lc[2]*(1-t)+gi[2]*t), 255)
            elif d <= 5.5:
                r0, g0, b0, _ = pixels[x, y]
                t = (d - 4.0) / 1.5
                pixels[x, y] = (int(gi[0]*(1-t)+r0*t), int(gi[1]*(1-t)+g0*t), int(gi[2]*(1-t)+b0*t), 255)
    add_noise(img, amount=3)
    img.save(os.path.join(BD, "photonic_injector_front_active.png"))
    print("  Created photonic_injector_front_active.png")

def make_photonic_injector_side():
    img = Image.new("RGBA", (16, 16), (60, 60, 65, 255))
    draw = ImageDraw.Draw(img)
    bc = (75, 75, 80, 255)
    for x in range(16):
        draw.point((x, 0), fill=bc)
        draw.point((x, 15), fill=bc)
    for y in range(16):
        draw.point((0, y), fill=bc)
        draw.point((15, y), fill=bc)
    add_noise(img, amount=3)
    img.save(os.path.join(BD, "photonic_injector_side.png"))
    print("  Created photonic_injector_side.png")

def make_photonic_injector_top():
    img = Image.new("RGBA", (16, 16), (55, 55, 60, 255))
    draw = ImageDraw.Draw(img)
    hc = (65, 65, 70, 255)
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0 or (x - y) % 4 == 0:
                draw.point((x, y), fill=hc)
    add_noise(img, amount=3)
    img.save(os.path.join(BD, "photonic_injector_top.png"))
    print("  Created photonic_injector_top.png")

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

def make_singularity_controller_gui():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    ox, oy = 0, 0
    draw_mc_bg(draw, ox, oy)
    ex, ey = ox+10, oy+16
    draw.rectangle([ex, ey, ex+15, ey+51], fill=(40, 40, 40, 255))
    draw.line([(ex, ey), (ex+15, ey)], fill=(55, 55, 55, 255))
    draw.line([(ex, ey), (ex, ey+51)], fill=(55, 55, 55, 255))
    draw.line([(ex, ey+51), (ex+15, ey+51)], fill=(255, 255, 255, 255))
    draw.line([(ex+15, ey), (ex+15, ey+51)], fill=(255, 255, 255, 255))
    sx, sy = ox+30, oy+16
    draw.rectangle([sx, sy, sx+9, sy+51], fill=(40, 40, 40, 255))
    draw.line([(sx, sy), (sx+9, sy)], fill=(55, 55, 55, 255))
    draw.line([(sx, sy), (sx, sy+51)], fill=(55, 55, 55, 255))
    draw.line([(sx, sy+51), (sx+9, sy+51)], fill=(255, 255, 255, 255))
    draw.line([(sx+9, sy), (sx+9, sy+51)], fill=(255, 255, 255, 255))
    draw_slot(draw, ox+56, oy+34)
    draw_slot(draw, ox+116, oy+34)
    draw_arrow_outline(draw, ox+79, oy+34)
    draw_inv(draw, ox, oy)
    draw.rectangle([176, 0, 191, 51], fill=(0, 100, 255, 255))
    draw.rectangle([192, 0, 201, 51], fill=(0, 200, 60, 255))
    draw_arrow_filled(draw, 176, 52)
    img.save(os.path.join(GD, "singularity_controller.png"))
    print("  Created singularity_controller.png (GUI)")

def make_photonic_injector_gui():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    ox, oy = 0, 0
    draw_mc_bg(draw, ox, oy)
    draw_slot(draw, ox+80, oy+34)
    draw.rectangle([ox+60, oy+56, ox+116, oy+66], fill=(185, 185, 185, 255))
    draw.line([(ox+60, oy+56), (ox+116, oy+56)], fill=(130, 130, 130, 255))
    draw.line([(ox+60, oy+56), (ox+60, oy+66)], fill=(130, 130, 130, 255))
    draw.line([(ox+60, oy+66), (ox+116, oy+66)], fill=(230, 230, 230, 255))
    draw.line([(ox+116, oy+56), (ox+116, oy+66)], fill=(230, 230, 230, 255))
    draw_inv(draw, ox, oy)
    img.save(os.path.join(GD, "photonic_injector.png"))
    print("  Created photonic_injector.png (GUI)")

if __name__ == "__main__":
    print("Generating block textures (16x16)...")
    make_singularity_casing()
    make_singularity_controller_front()
    make_singularity_controller_front_formed()
    make_singularity_port()
    make_exotic_matter_core()
    make_photonic_injector_front()
    make_photonic_injector_front_active()
    make_photonic_injector_side()
    make_photonic_injector_top()
    print()
    print("Generating GUI textures (256x256)...")
    make_singularity_controller_gui()
    make_photonic_injector_gui()
    print()
    print("All textures generated successfully!")
