import json
import math

radius = 16
shell_min = 15.5
shell_max = 16.5
center = radius  # center layer index
height = 2 * radius + 1  # 33

layers = []
total_shell = 0
total_air = 0

for y_idx in range(height):
    dy = y_idx - center  # -16 to +16
    
    shell = []
    air = []
    
    for x in range(-radius, radius + 1):
        for z in range(-radius, radius + 1):
            d = math.sqrt(x*x + dy*dy + z*z)
            
            if shell_min <= d <= shell_max:
                shell.append([x, z])
            elif d < shell_min:
                # Inside the sphere - should be air
                # But [0,0] on equator layer goes to shell (for core block)
                if x == 0 and z == 0 and dy == 0:
                    shell.append([x, z])
                else:
                    air.append([x, z])
    
    # Sort for consistent ordering
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
