import json

data = json.load(open('src/main/resources/data/tachyon/multiblock/singularity_engine.json'))

# Check top layer (y=32) for shell position at [0,0]
top = [l for l in data['layers'] if l['y'] == 32]
if top:
    layer = top[0]
    print('y=32 shell count:', len(layer['shell']))
    print('[0,0] in shell:', [0, 0] in layer['shell'])
    print('Accepts:', layer.get('accepts', 'none'))

# Also check the "top of sphere" - what y has the topmost shell with [0,0]?
for l in data['layers']:
    if [0, 0] in l['shell']:
        print(f"[0,0] is SHELL at y={l['y']}, accepts={l.get('accepts','?')}")

# Check radius at equator (y=16): what's the max x offset?
equator = [l for l in data['layers'] if l['y'] == 16][0]
max_x = max(abs(s[0]) for s in equator['shell'])
max_z = max(abs(s[1]) for s in equator['shell'])
print(f'Equator (y=16) max |x|={max_x}, max |z|={max_z}')

# Check what's at [0, radius] on equator layer
radius = data['height'] // 2  # 16
print(f'Radius = {radius}')
print(f'[{radius},0] in equator shell:', [radius, 0] in equator['shell'])
print(f'[0,{radius}] in equator shell:', [0, radius] in equator['shell'])
print(f'[0,{-radius}] in equator shell:', [0, -radius] in equator['shell'])
print(f'[-{radius},0] in equator shell:', [-radius, 0] in equator['shell'])

# Check top: [0,0] at y = radius + radius = 32 (top pole)
# The energy output port should go at the top pole
print(f'[0,0] at y=32 (top):', [0, 0] in top[0]['shell'] if top else 'no layer')
