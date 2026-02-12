import math, json

R = 4.5
r = 2.5
hollow_r = 1.5
height = 5
y_center = (height - 1) / 2.0

shell = {}
air = {}

for y in range(height):
    dy = y - y_center
    shell_layer = []
    air_layer = []
    for x in range(-8, 9):
        for z in range(-8, 9):
            h = math.sqrt(x*x + z*z)
            d = math.sqrt((h - R)**2 + dy**2)
            if d < r:
                if d < hollow_r:
                    air_layer.append([x, z])
                else:
                    shell_layer.append([x, z])
    shell[str(y)] = sorted(shell_layer, key=lambda p: (p[0], p[1]))
    if air_layer:
        air[str(y)] = sorted(air_layer, key=lambda p: (p[0], p[1]))

ring_path = []
seen = set()
for i in range(360):
    theta = 2 * math.pi * i / 360
    x = round(R * math.cos(theta))
    z = round(R * math.sin(theta))
    if (x, z) not in seen:
        seen.add((x, z))
        ring_path.append([x, z])

print("SHELL POSITIONS")
for y in range(height):
    print(f"Layer {y}: {len(shell[str(y)])} positions")

print()
print("AIR POSITIONS")
for y in range(height):
    if str(y) in air:
        print(f"Layer {y}: {len(air[str(y)])} positions")
    else:
        print(f"Layer {y}: 0 positions")

print()
print(f"RING PATH: {len(ring_path)} positions")

print()
print("CROSS-SECTION at z=0")
for y in range(height-1, -1, -1):
    row = ""
    for x in range(-7, 8):
        if [x, 0] in shell[str(y)]:
            row += "#"
        elif str(y) in air and [x, 0] in air[str(y)]:
            row += "."
        else:
            row += " "
    print(f"y={y}: {row}")

ctrl = [-5, 0]
print()
print(f"Controller at {ctrl}:")
for y in range(height):
    if ctrl in shell[str(y)]:
        print(f"  y={y}: SHELL")
    elif str(y) in air and ctrl in air[str(y)]:
        print(f"  y={y}: AIR")
    else:
        print(f"  y={y}: OUTSIDE")

# Output JSON
result = {
    "controller_offset": [-5, 0],
    "height": 5,
    "shell": shell,
    "air": air,
    "ring_path": ring_path
}

with open("torus_positions.json", "w") as f:
    json.dump(result, f, indent=2)

print()
print("Wrote torus_positions.json")
