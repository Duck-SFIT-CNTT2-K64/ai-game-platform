"""
Convert JPG sprite assets to PNG with transparent backgrounds using pure PIL (no numpy).
"""
import os
from PIL import Image

BASE = r"d:\nam_3_ki_2\tri_tue_nhan_tao\ai-game-platform\src\main\resources\image"
PIXEL_ANIM = os.path.join(BASE, "pixel_animation")

def remove_checkerboard_bg_pil(img_path, out_path, threshold=30):
    img = Image.open(img_path).convert('RGBA')
    pixels = img.load()
    w, h = img.size
    
    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            
            # Detect "checkerboard" pixels: high brightness, low saturation (gray/white)
            max_c = max(r, g, b)
            min_c = min(r, g, b)
            saturation = max_c - min_c
            brightness = (r + g + b) / 3.0
            
            # A pixel is "background" if it has very low saturation AND is bright
            if saturation < threshold and brightness > 170:
                pixels[x, y] = (r, g, b, 0)  # transparent
                
    img.save(out_path)
    print(f"  Converted: {os.path.basename(img_path)} -> {os.path.basename(out_path)}")

def convert_to_png_simple(img_path, out_path):
    img = Image.open(img_path).convert('RGBA')
    img.save(out_path)
    print(f"  Converted: {os.path.basename(img_path)} -> {os.path.basename(out_path)}")

sprites_to_convert = [
    ("Duck.jpg", "Duck.png"),
    ("Item.jpg", "Item.png"),
    ("Item_when_picked.jpg", "Item_when_picked.png"),
    ("Flag.jpg", "Flag.png"),
    ("Stop.jpg", "Stop.png"),
]

print("=== Converting sprites (with background removal) ===")
for src, dst in sprites_to_convert:
    src_path = os.path.join(PIXEL_ANIM, src)
    dst_path = os.path.join(PIXEL_ANIM, dst)
    if os.path.exists(src_path):
        remove_checkerboard_bg_pil(src_path, dst_path)
    else:
        print(f"  MISSING: {src}")

print("\n=== Converting duck_hurt (dark background) ===")
hurt_src = os.path.join(PIXEL_ANIM, "duck_hurt.jpg")
hurt_dst = os.path.join(PIXEL_ANIM, "duck_hurt.png")
if os.path.exists(hurt_src):
    convert_to_png_simple(hurt_src, hurt_dst)

print("\n=== Converting FinishLine ===")
for i in range(1, 4):
    src = os.path.join(PIXEL_ANIM, f"FinishLine_{i}.jpg")
    dst = os.path.join(PIXEL_ANIM, f"FinishLine_{i}.png")
    if os.path.exists(src):
        convert_to_png_simple(src, dst)

print("\n=== Converting tiles ===")
tiles_to_convert = [
    ("Grass_1.jpg", "Grass_1.png"),
    ("Grass_2.jpg", "Grass_2.png"),
    ("Water.jpg", "Water.png"),
]
for src, dst in tiles_to_convert:
    src_path = os.path.join(PIXEL_ANIM, src)
    dst_path = os.path.join(PIXEL_ANIM, dst)
    if os.path.exists(src_path):
        convert_to_png_simple(src_path, dst_path)

print("\n=== Converting walk frames ===")
walk_dir = os.path.join(PIXEL_ANIM, "duck_walk_frames")
if os.path.exists(walk_dir):
    for f in sorted(os.listdir(walk_dir)):
        if f.endswith('.jpg') or f.endswith('.png'):
            src_path = os.path.join(walk_dir, f)
            dst_name = os.path.splitext(f)[0] + '.png'
            dst_path = os.path.join(walk_dir, dst_name)
            remove_checkerboard_bg_pil(src_path, dst_path)

print("\n=== Converting idle frames ===")
idle_dir = os.path.join(PIXEL_ANIM, "duck_idle_frames")
if os.path.exists(idle_dir):
    for f in sorted(os.listdir(idle_dir)):
        if f.endswith('.png'):
            src_path = os.path.join(idle_dir, f)
            remove_checkerboard_bg_pil(src_path, src_path)

print("\nDone! All assets converted.")
