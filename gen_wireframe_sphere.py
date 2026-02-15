import json
import math

radius = 16
shell_min = 15.5
shell_max = 16.5
ring_width = 1  # blocks within this distance of a plane are part of ring
center = radius
height = 2 * radius + 1

layers = []
total_shell = 0
total_air = 0

for y_idx in range(height):
    dy = y_idx - center
    
    shell = []
    air = []
    
    for x in range(-radius, radius + 1):
        for z in range(-radius, radius + 1):
            d = math.sqrt(x*x + dy*dy + z*z)
            
            if shell_min <= d <= shell_max:
                # On the sphere surface - check if on a ring
                on_equator = abs(dy) <= ring_width  # horizontal ring
                on_meridian_xz = abs(z) <= ring_width  # vertical ring in XZ plane
                on_meridian_yz = abs(x) <= ring_width  # vertical ring in YZ plane
                
                if on_equator or on_meridian_xz or on_meridian_yz:
                    shell.append([x, z])
                # else: not on any ring, skip (not shell, not air - it's the outer shell gap)
            elif d < shell_min:
                # Inside sphere
                if x == 0 and z == 0 and dy == 0:
                    shell.append([x, z])  # center core block
                else:
                    air.append([x, z])
    
    shell.sort(key=lambda p: (p[0], p[1]))
    air.sort(key=lambda p: (p[0], p[1]))
    
    total_shell += len(shell)
    total_air += len(air)
    
    layer = {
        "y": y_idx,
        "shell": shell,
        "air": air,
        "accepts": [
            "tachyon:singularity_casing",
            "tachyon:singularity_port",
            "tachyon:exotic_matter_core",
            "tachyon:singularity_controller"
        ]
    }
    layers.append(layer)

data = {
    "controller_offset": [-radius, 0, 0],
    "height": height,
    "required_blocks": {
        "tachyon:exotic_matter_core": 1
    },
    "layers": layers
}

print(f"Total shell blocks: {total_shell}")
print(f"Total air blocks: {total_air}")
print(f"Height: {height}")

with open(r"d:\MCMODDING\tachyon\src\main\resources\data\tachyon\multiblock\singularity_engine.json", "w") as f:
    json.dump(data, f, indent=2)

print("Done!")
