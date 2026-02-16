"""
Generate textures for Ore Crusher, Alloy Forge machines, their GUIs,
and 7 crushed ore item textures for the Tachyon Minecraft mod.
All textures use PIL (Pillow).
"""

from PIL import Image, ImageDraw
import os
import random

BASE = r"d:\MCMODDING\tachyon\src\main\resources\assets\tachyon\textures"

def ensure_dir(path):
    os.makedirs(os.path.dirname(path), exist_ok=True)

def save(img, rel_path):
    full = os.path.join(BASE, rel_path)
    ensure_dir(full)
    img.save(full)
    print(f"  Saved: {rel_path}")

# ---------------------------------------------------------------------------
# Utility helpers
# ---------------------------------------------------------------------------

def lerp_color(c1, c2, t):
    """Linearly interpolate between two RGB(A) tuples."""
    return tuple(int(a + (b - a) * t) for a, b in zip(c1, c2))

def add_noise(img, amount=8):
    """Add subtle per-pixel noise to make textures less flat."""
    rng = random.Random(42)
    px = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            c = px[x, y]
            if len(c) == 4 and c[3] == 0:
                continue
            d = rng.randint(-amount, amount)
            if len(c) == 4:
                px[x, y] = (max(0, min(255, c[0]+d)),
                             max(0, min(255, c[1]+d)),
                             max(0, min(255, c[2]+d)),
                             c[3])
            else:
                px[x, y] = (max(0, min(255, c[0]+d)),
                             max(0, min(255, c[1]+d)),
                             max(0, min(255, c[2]+d)))

def draw_border_16(draw, border_color):
    """Draw a 1px machine border on a 16x16 image (darker edge frame)."""
    # Top and bottom rows
    for x in range(16):
        draw.point((x, 0), fill=border_color)
        draw.point((x, 15), fill=border_color)
    # Left and right columns
    for y in range(16):
        draw.point((0, y), fill=border_color)
        draw.point((15, y), fill=border_color)


# ===========================================================================
# BLOCK TEXTURES — ORE CRUSHER (grey / iron colored)
# ===========================================================================

def ore_crusher_front(active=False):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Base grey fill
    base = (140, 140, 140, 255)
    dark_border = (70, 70, 70, 255)
    light_edge = (170, 170, 170, 255)

    # Fill base
    draw.rectangle([0, 0, 15, 15], fill=base)

    # Border: dark top/left, light bottom/right (Minecraft style bevel)
    for i in range(16):
        draw.point((i, 0), fill=dark_border)
        draw.point((0, i), fill=dark_border)
        draw.point((i, 15), fill=light_edge)
        draw.point((15, i), fill=light_edge)
    draw.point((0, 15), fill=dark_border)
    draw.point((15, 0), fill=dark_border)

    # Crusher jaw area — darker recessed rectangle
    jaw_bg = (55, 55, 55, 255)
    draw.rectangle([3, 4, 12, 12], fill=jaw_bg)

    # Jaw teeth — two triangular shapes meeting in the middle
    jaw_color = (100, 100, 100, 255)

    # Left jaw (triangle pointing right)
    # Rows from y=5 to y=11, the left jaw comes from left
    for row_i, y in enumerate(range(5, 12)):
        mid = 7  # center
        dist_from_center = abs(y - 8)
        left_end = 3
        right_end = mid - dist_from_center
        if right_end >= left_end:
            draw.line([(left_end, y), (right_end, y)], fill=jaw_color)

    # Right jaw (triangle pointing left)
    for row_i, y in enumerate(range(5, 12)):
        mid = 8
        dist_from_center = abs(y - 8)
        left_start = mid + dist_from_center
        right_end = 12
        if left_start <= right_end:
            draw.line([(left_start, y), (right_end, y)], fill=jaw_color)

    # Active glow between the jaws
    if active:
        glow_colors = [
            (255, 200, 50, 255),
            (255, 160, 30, 255),
            (255, 120, 20, 255),
        ]
        # The gap between the jaws (center column area)
        for y in range(5, 12):
            dist_from_center = abs(y - 8)
            left_jaw_end = 7 - dist_from_center
            right_jaw_start = 8 + dist_from_center
            # Fill the gap
            for x in range(left_jaw_end + 1, right_jaw_start):
                if 3 <= x <= 12:
                    ci = min(len(glow_colors)-1, abs(y-8))
                    draw.point((x, y), fill=glow_colors[ci])

    # Small bolt details on the frame
    bolt = (90, 90, 90, 255)
    draw.point((2, 2), fill=bolt)
    draw.point((13, 2), fill=bolt)
    draw.point((2, 13), fill=bolt)
    draw.point((13, 13), fill=bolt)

    add_noise(img, 5)
    return img


def ore_crusher_side():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    base = (140, 140, 140, 255)
    dark_border = (70, 70, 70, 255)
    light_edge = (170, 170, 170, 255)

    draw.rectangle([0, 0, 15, 15], fill=base)

    # Bevel border
    for i in range(16):
        draw.point((i, 0), fill=dark_border)
        draw.point((0, i), fill=dark_border)
        draw.point((i, 15), fill=light_edge)
        draw.point((15, i), fill=light_edge)
    draw.point((0, 15), fill=dark_border)
    draw.point((15, 0), fill=dark_border)

    # Horizontal panel lines
    panel_line = (115, 115, 115, 255)
    draw.line([(1, 5), (14, 5)], fill=panel_line)
    draw.line([(1, 10), (14, 10)], fill=panel_line)

    # Rivet/bolt pattern
    bolt_dark = (90, 90, 90, 255)
    bolt_light = (175, 175, 175, 255)
    bolt_positions = [(3, 3), (12, 3), (3, 7), (12, 7), (3, 12), (12, 12)]
    for bx, by in bolt_positions:
        draw.point((bx, by), fill=bolt_dark)
        draw.point((bx+1, by), fill=bolt_light)

    add_noise(img, 5)
    return img


def ore_crusher_top():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    base = (145, 145, 145, 255)
    dark_border = (70, 70, 70, 255)
    light_edge = (175, 175, 175, 255)

    draw.rectangle([0, 0, 15, 15], fill=base)

    # Bevel
    for i in range(16):
        draw.point((i, 0), fill=dark_border)
        draw.point((0, i), fill=dark_border)
        draw.point((i, 15), fill=light_edge)
        draw.point((15, i), fill=light_edge)
    draw.point((0, 15), fill=dark_border)
    draw.point((15, 0), fill=dark_border)

    # Hopper opening in center
    hopper_border = (60, 60, 60, 255)
    hopper_inside = (40, 40, 40, 255)
    draw.rectangle([5, 5, 10, 10], fill=hopper_inside)
    draw.rectangle([5, 5, 10, 10], outline=hopper_border)

    # Inner funnel lines (diagonal)
    funnel = (80, 80, 80, 255)
    draw.point((6, 6), fill=funnel)
    draw.point((9, 6), fill=funnel)
    draw.point((6, 9), fill=funnel)
    draw.point((9, 9), fill=funnel)

    # Corner bolts
    bolt = (100, 100, 100, 255)
    draw.point((2, 2), fill=bolt)
    draw.point((13, 2), fill=bolt)
    draw.point((2, 13), fill=bolt)
    draw.point((13, 13), fill=bolt)

    add_noise(img, 5)
    return img


# ===========================================================================
# BLOCK TEXTURES — ALLOY FORGE (dark grey / orange)
# ===========================================================================

def alloy_forge_front(active=False):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    base = (85, 85, 90, 255)
    dark_border = (45, 45, 48, 255)
    light_edge = (110, 110, 115, 255)

    draw.rectangle([0, 0, 15, 15], fill=base)

    # Bevel
    for i in range(16):
        draw.point((i, 0), fill=dark_border)
        draw.point((0, i), fill=dark_border)
        draw.point((i, 15), fill=light_edge)
        draw.point((15, i), fill=light_edge)
    draw.point((0, 15), fill=dark_border)
    draw.point((15, 0), fill=dark_border)

    # Furnace opening (dark rectangle)
    opening_border = (35, 30, 25, 255)
    if active:
        opening_fill = (30, 20, 15, 255)
    else:
        opening_fill = (25, 25, 25, 255)

    draw.rectangle([4, 6, 11, 12], fill=opening_fill)
    draw.rectangle([4, 6, 11, 12], outline=opening_border)

    if active:
        # Bright orange/molten glow inside the opening
        glow_bright = (255, 170, 40, 255)
        glow_mid = (255, 120, 20, 255)
        glow_dark = (200, 80, 10, 255)

        # Bottom row brightest (molten metal), fading up
        for y in range(7, 12):
            intensity = (y - 7) / 4.0  # 0 at top, 1 at bottom
            c = lerp_color(glow_dark[:3], glow_bright[:3], intensity)
            for x in range(5, 11):
                draw.point((x, y), fill=c + (255,))

        # Some yellow/white hotspots at bottom
        draw.point((7, 11), fill=(255, 220, 100, 255))
        draw.point((8, 11), fill=(255, 230, 120, 255))
        draw.point((7, 10), fill=(255, 190, 60, 255))
    else:
        # Just dark interior with subtle depth
        for y in range(7, 12):
            for x in range(5, 11):
                depth = 20 + (y - 7) * 3
                draw.point((x, y), fill=(depth, depth, depth, 255))

    # Orange accent stripe at top
    accent = (180, 100, 30, 255)
    draw.line([(2, 3), (13, 3)], fill=accent)
    draw.line([(2, 4), (13, 4)], fill=(160, 85, 25, 255))

    # Bolts
    bolt = (60, 60, 65, 255)
    draw.point((2, 2), fill=bolt)
    draw.point((13, 2), fill=bolt)
    draw.point((2, 13), fill=bolt)
    draw.point((13, 13), fill=bolt)

    add_noise(img, 4)
    return img


def alloy_forge_side():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    base = (85, 85, 90, 255)
    dark_border = (45, 45, 48, 255)
    light_edge = (110, 110, 115, 255)

    draw.rectangle([0, 0, 15, 15], fill=base)

    # Bevel
    for i in range(16):
        draw.point((i, 0), fill=dark_border)
        draw.point((0, i), fill=dark_border)
        draw.point((i, 15), fill=light_edge)
        draw.point((15, i), fill=light_edge)
    draw.point((0, 15), fill=dark_border)
    draw.point((15, 0), fill=dark_border)

    # Heat vent slits (horizontal dark lines)
    vent_dark = (40, 40, 42, 255)
    vent_light = (95, 95, 100, 255)
    for vy in [4, 6, 8, 10, 12]:
        draw.line([(3, vy), (12, vy)], fill=vent_dark)
        draw.line([(3, vy+1), (12, vy+1)], fill=vent_light)

    # Orange accent at top
    accent = (180, 100, 30, 255)
    draw.line([(2, 2), (13, 2)], fill=accent)

    # Bolts at corners
    bolt = (60, 60, 65, 255)
    draw.point((2, 1), fill=bolt)
    draw.point((13, 1), fill=bolt)
    draw.point((2, 14), fill=bolt)
    draw.point((13, 14), fill=bolt)

    add_noise(img, 4)
    return img


def alloy_forge_top():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    base = (80, 80, 85, 255)
    dark_border = (45, 45, 48, 255)
    light_edge = (105, 105, 110, 255)

    draw.rectangle([0, 0, 15, 15], fill=base)

    # Bevel
    for i in range(16):
        draw.point((i, 0), fill=dark_border)
        draw.point((0, i), fill=dark_border)
        draw.point((i, 15), fill=light_edge)
        draw.point((15, i), fill=light_edge)
    draw.point((0, 15), fill=dark_border)
    draw.point((15, 0), fill=dark_border)

    # Chimney/vent pattern — square chimney opening
    chimney_outer = (50, 50, 53, 255)
    chimney_inner = (30, 30, 32, 255)
    chimney_grate = (65, 65, 68, 255)

    draw.rectangle([5, 5, 10, 10], fill=chimney_inner)
    draw.rectangle([5, 5, 10, 10], outline=chimney_outer)

    # Grate lines across chimney
    for x in range(6, 10):
        draw.point((x, 7), fill=chimney_grate)
    for y in range(6, 10):
        draw.point((7, y), fill=chimney_grate)

    # Subtle orange glow at chimney center
    draw.point((7, 7), fill=(120, 60, 20, 255))
    draw.point((8, 7), fill=(100, 50, 15, 255))
    draw.point((7, 8), fill=(100, 50, 15, 255))
    draw.point((8, 8), fill=(110, 55, 18, 255))

    # Corner bolts
    bolt = (60, 60, 65, 255)
    draw.point((2, 2), fill=bolt)
    draw.point((13, 2), fill=bolt)
    draw.point((2, 13), fill=bolt)
    draw.point((13, 13), fill=bolt)

    add_noise(img, 4)
    return img


# ===========================================================================
# GUI TEXTURE HELPERS
# ===========================================================================

# Standard Minecraft GUI colors
GUI_BG = (198, 198, 198)
GUI_BORDER_DARK = (55, 55, 55)
GUI_BORDER_MID = (139, 139, 139)
GUI_SLOT_DARK = (55, 55, 55)      # top-left inner border of slot
GUI_SLOT_LIGHT = (255, 255, 255)   # bottom-right inner highlight of slot
GUI_SLOT_BG = (139, 139, 139)     # slot interior


def draw_gui_base(draw, w=176, h=166):
    """Draw the standard MC-style GUI background with 3D border."""
    # Outer border frame (dark)
    draw.rectangle([0, 0, w-1, h-1], fill=GUI_BG)

    # Top border line
    draw.line([(0, 0), (w-1, 0)], fill=GUI_BORDER_DARK)
    # Left border line
    draw.line([(0, 0), (0, h-1)], fill=GUI_BORDER_DARK)
    # Bottom border line
    draw.line([(0, h-1), (w-1, h-1)], fill=GUI_BORDER_DARK)
    # Right border line
    draw.line([(w-1, 0), (w-1, h-1)], fill=GUI_BORDER_DARK)

    # Second border: white/light on top-left inner, dark on bottom-right inner
    # Light top inner
    draw.line([(1, 1), (w-2, 1)], fill=(255, 255, 255))
    draw.line([(1, 1), (1, h-2)], fill=(255, 255, 255))
    # Dark bottom-right inner
    draw.line([(2, h-2), (w-2, h-2)], fill=GUI_BORDER_MID)
    draw.line([(w-2, 2), (w-2, h-2)], fill=GUI_BORDER_MID)

    # Third inner border
    draw.line([(2, 2), (w-3, 2)], fill=GUI_BORDER_MID)
    draw.line([(2, 2), (2, h-3)], fill=GUI_BORDER_MID)
    draw.line([(3, h-3), (w-3, h-3)], fill=(255, 255, 255))
    draw.line([(w-3, 3), (w-3, h-3)], fill=(255, 255, 255))


def draw_slot(draw, x, y, size=18):
    """Draw a standard MC inventory slot at (x, y) with given size."""
    # The slot is size x size; the outer pixel is a border
    # Dark top and left edge
    draw.line([(x, y), (x + size - 1, y)], fill=GUI_SLOT_DARK)
    draw.line([(x, y), (x, y + size - 1)], fill=GUI_SLOT_DARK)
    # Light bottom and right edge
    draw.line([(x, y + size - 1), (x + size - 1, y + size - 1)], fill=GUI_SLOT_LIGHT)
    draw.line([(x + size - 1, y), (x + size - 1, y + size - 1)], fill=GUI_SLOT_LIGHT)
    # Interior
    draw.rectangle([x + 1, y + 1, x + size - 2, y + size - 2], fill=GUI_SLOT_BG)


def draw_inventory_slots(draw, start_x=7, inv_y=83, hotbar_y=141):
    """Draw the standard 9x3 inventory + 9 hotbar slots."""
    # Main inventory: 9 columns x 3 rows
    for row in range(3):
        for col in range(9):
            sx = start_x + col * 18
            sy = inv_y + row * 18
            draw_slot(draw, sx, sy)

    # Hotbar: 9 slots
    for col in range(9):
        sx = start_x + col * 18
        draw_slot(draw, sx, hotbar_y)


def draw_energy_bar_outline(draw, x, y, w=16, h=52):
    """Draw the energy bar border area."""
    # Outer dark border
    draw.rectangle([x, y, x + w - 1, y + h - 1], outline=GUI_SLOT_DARK)
    # Inner lighter border
    draw.rectangle([x + 1, y + 1, x + w - 2, y + h - 2], fill=GUI_SLOT_BG)


def draw_energy_bar_fill(draw, x, y, w=16, h=52):
    """Draw the energy bar fill sprite (red gradient) for UV area."""
    for row in range(h):
        # Gradient from bright red at bottom to dark red at top
        t = row / max(1, h - 1)  # 0 at top, 1 at bottom
        r = int(180 + 75 * t)
        g = int(20 + 30 * t)
        b = int(20 + 10 * t)
        draw.line([(x, y + row), (x + w - 1, y + row)], fill=(r, g, b))


def draw_progress_arrow(draw, x, y):
    """Draw a 24x17 right-pointing arrow for the progress indicator (UV area)."""
    # White arrow on transparent — we draw in white
    arrow_color = (255, 255, 255)
    # Arrow body (rectangle part): 16 wide, 7 tall, centered vertically
    body_top = y + 5
    body_bot = y + 11
    draw.rectangle([x, body_top, x + 15, body_bot], fill=arrow_color)

    # Arrow head (triangle): from x+16 to x+23, pointing right
    # Center at y+8
    for col in range(8):
        half_h = 8 - col  # starts at 8 tall, narrows to 1
        top = y + 8 - half_h
        bot = y + 8 + half_h
        draw.line([(x + 16 + col, top), (x + 16 + col, bot)], fill=arrow_color)


def draw_upgrade_slots(draw, positions):
    """Draw smaller upgrade slots at given (x,y) positions."""
    for sx, sy in positions:
        draw_slot(draw, sx, sy, size=18)


# ===========================================================================
# GUI — ORE CRUSHER
# ===========================================================================

def gui_ore_crusher():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Draw the 176x166 GUI area
    draw_gui_base(draw, 176, 166)

    # Title area: "Ore Crusher" text would go here but we just leave background

    # Energy bar outline at (10, 16), 16x52
    draw_energy_bar_outline(draw, 10, 16, 16, 52)

    # Input slot at (55, 34)
    draw_slot(draw, 55, 34)

    # Output slot at (115, 34)
    draw_slot(draw, 115, 34)

    # Progress arrow area at (79, 34) — leave as background (filled by UV)
    # Just draw a subtle outline to show where it goes
    arrow_bg = (185, 185, 185)
    draw.rectangle([79, 35, 102, 51], fill=arrow_bg)
    # Draw a faint grey arrow shape as background indicator
    faint = (175, 175, 175)
    for col in range(16):
        draw.line([(80 + col, 39), (80 + col, 47)], fill=faint)
    for col in range(6):
        half = 6 - col
        draw.line([(96 + col, 43 - half), (96 + col, 43 + half)], fill=faint)

    # 3 upgrade slots in horizontal row at (61, 61), (79, 61), (97, 61)
    draw_upgrade_slots(draw, [(61, 61), (79, 61), (97, 61)])

    # Player inventory + hotbar
    draw_inventory_slots(draw, start_x=7, inv_y=83, hotbar_y=141)

    # --- UV extras (right of the 176px main area) ---

    # Energy bar fill at (176, 0), size 16x52 — red gradient
    draw_energy_bar_fill(draw, 176, 0, 16, 52)

    # Progress arrow at (176, 52), size 24x17
    draw_progress_arrow(draw, 176, 52)

    return img


# ===========================================================================
# GUI — ALLOY FORGE
# ===========================================================================

def gui_alloy_forge():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Draw the 176x166 GUI area
    draw_gui_base(draw, 176, 166)

    # Energy bar outline at (10, 16), 16x52
    draw_energy_bar_outline(draw, 10, 16, 16, 52)

    # Two input slots stacked
    draw_slot(draw, 51, 26)  # top input
    draw_slot(draw, 51, 46)  # bottom input

    # Output slot at (115, 34)
    draw_slot(draw, 115, 34)

    # Progress arrow area at (79, 34)
    arrow_bg = (185, 185, 185)
    draw.rectangle([79, 35, 102, 51], fill=arrow_bg)
    faint = (175, 175, 175)
    for col in range(16):
        draw.line([(80 + col, 39), (80 + col, 47)], fill=faint)
    for col in range(6):
        half = 6 - col
        draw.line([(96 + col, 43 - half), (96 + col, 43 + half)], fill=faint)

    # 3 upgrade slots at (61, 61), (79, 61), (97, 61)
    draw_upgrade_slots(draw, [(61, 61), (79, 61), (97, 61)])

    # Player inventory + hotbar
    draw_inventory_slots(draw, start_x=7, inv_y=83, hotbar_y=141)

    # --- UV extras ---
    # Energy bar fill at (176, 0), size 16x52
    draw_energy_bar_fill(draw, 176, 0, 16, 52)

    # Progress arrow at (176, 52), size 24x17
    draw_progress_arrow(draw, 176, 52)

    return img


# ===========================================================================
# CRUSHED ORE ITEM TEXTURES
# ===========================================================================

def make_crushed_ore(base_color, highlight_color, shadow_color):
    """
    Create a 16x16 dust/powder pile texture.
    Pile sits in the bottom-center with scattered pixels above.
    """
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    rng = random.Random(hash(base_color))

    # Main pile shape — roughly in bottom center (rows 10-14, cols 4-11)
    # Row by row, wider at the bottom
    pile_rows = {
        14: range(5, 11),    # bottom row — widest
        13: range(4, 12),    # second from bottom — widest
        12: range(5, 11),    # narrowing
        11: range(6, 10),    # narrowing more
        10: range(7, 9),     # small top of pile
    }

    for y, xs in pile_rows.items():
        for x in xs:
            # Determine shade: use shadow for left/top, highlight for right/bottom
            if x <= min(xs) or y <= 10:
                c = shadow_color
            elif x >= max(xs) - 1 and y >= 13:
                c = highlight_color
            else:
                c = base_color
            draw.point((x, y), fill=c + (255,))

    # Scattered particles above the pile (floating dust)
    scatter_positions = []
    for _ in range(12):
        sx = rng.randint(4, 11)
        sy = rng.randint(5, 9)
        scatter_positions.append((sx, sy))

    for sx, sy in scatter_positions:
        # Use base or highlight randomly
        c = highlight_color if rng.random() > 0.5 else base_color
        alpha = rng.randint(150, 230)
        draw.point((sx, sy), fill=c + (alpha,))

    # A few more sparse particles higher up
    for _ in range(5):
        sx = rng.randint(5, 10)
        sy = rng.randint(3, 6)
        c = base_color
        alpha = rng.randint(100, 180)
        draw.point((sx, sy), fill=c + (alpha,))

    return img


# ===========================================================================
# MAIN — Generate everything
# ===========================================================================

def main():
    print("=== Generating Ore Crusher block textures ===")
    save(ore_crusher_front(active=False), "block/ore_crusher_front.png")
    save(ore_crusher_front(active=True), "block/ore_crusher_front_active.png")
    save(ore_crusher_side(), "block/ore_crusher_side.png")
    save(ore_crusher_top(), "block/ore_crusher_top.png")

    print("\n=== Generating Alloy Forge block textures ===")
    save(alloy_forge_front(active=False), "block/alloy_forge_front.png")
    save(alloy_forge_front(active=True), "block/alloy_forge_front_active.png")
    save(alloy_forge_side(), "block/alloy_forge_side.png")
    save(alloy_forge_top(), "block/alloy_forge_top.png")

    print("\n=== Generating GUI textures ===")
    save(gui_ore_crusher(), "gui/ore_crusher.png")
    save(gui_alloy_forge(), "gui/alloy_forge.png")

    print("\n=== Generating Crushed Ore item textures ===")

    # Crushed Titanium — silver-blue
    save(make_crushed_ore(
        base_color=(160, 175, 200),
        highlight_color=(200, 215, 235),
        shadow_color=(110, 125, 155)
    ), "item/crushed_titanium.png")

    # Crushed Tungsten — dark grey
    save(make_crushed_ore(
        base_color=(95, 95, 100),
        highlight_color=(140, 140, 148),
        shadow_color=(60, 60, 65)
    ), "item/crushed_tungsten.png")

    # Crushed Lithium — light silver/white
    save(make_crushed_ore(
        base_color=(210, 215, 220),
        highlight_color=(240, 245, 250),
        shadow_color=(170, 175, 180)
    ), "item/crushed_lithium.png")

    # Crushed Thorium — dark green
    save(make_crushed_ore(
        base_color=(60, 120, 55),
        highlight_color=(90, 160, 80),
        shadow_color=(35, 80, 30)
    ), "item/crushed_thorium.png")

    # Crushed Iron — brown/rusty
    save(make_crushed_ore(
        base_color=(165, 115, 75),
        highlight_color=(200, 150, 105),
        shadow_color=(120, 80, 50)
    ), "item/crushed_iron.png")

    # Crushed Gold — yellow/gold
    save(make_crushed_ore(
        base_color=(230, 195, 50),
        highlight_color=(255, 230, 100),
        shadow_color=(180, 145, 25)
    ), "item/crushed_gold.png")

    # Crushed Copper — orange/copper
    save(make_crushed_ore(
        base_color=(200, 120, 60),
        highlight_color=(235, 160, 95),
        shadow_color=(150, 85, 35)
    ), "item/crushed_copper.png")

    print("\n=== All textures generated successfully! ===")


if __name__ == "__main__":
    main()
