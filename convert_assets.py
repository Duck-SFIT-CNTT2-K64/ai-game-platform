"""
Convert JPG sprite assets to PNG with transparent backgrounds.
The JPG files have a fake checkerboard pattern (gray/white) that simulates
transparency, but since JPGs don't support alpha, we need to remove it.
For tiles (Grass, Water) we keep them as-is since they fill the entire cell.
"""
import os
from PIL import Image
import numpy as np

BASE = r"d:\nam_3_ki_2\tri_tue_nhan_tao\ai-game-platform\src\main\resources\image"
PIXEL_ANIM = os.path.join(BASE, "pixel_animation")


def remove_checkerboard_bg(img_path, out_path, threshold=30):
    """Remove the checkerboard transparency pattern from JPG sprites.
    
    The checkerboard consists of alternating light-gray (#C0C0C0 ≈ 192,192,192)
    and white (#FFFFFF = 255,255,255) squares, plus near-white transition pixels.
    We detect pixels that are close to these colors and make them transparent.
    """
    img = Image.open(img_path).convert('RGBA')
    data = np.array(img)

    r, g, b, a = data[:,:,0], data[:,:,1], data[:,:,2], data[:,:,3]

    # Detect "checkerboard" pixels: high brightness, low saturation (gray/white)
    max_channel = np.maximum(np.maximum(r.astype(int), g.astype(int)), b.astype(int))
    min_channel = np.minimum(np.minimum(r.astype(int), g.astype(int)), b.astype(int))
    saturation = max_channel - min_channel  # Close to 0 for grays/whites
    brightness = (r.astype(int) + g.astype(int) + b.astype(int)) / 3.0

    # A pixel is "background" if it has very low saturation AND is bright
    is_bg = (saturation < threshold) & (brightness > 170)

    data[is_bg, 3] = 0  # Make background transparent

    result = Image.fromarray(data, 'RGBA')
    result.save(out_path)
    print(f"  Converted: {os.path.basename(img_path)} -> {os.path.basename(out_path)}")


def convert_to_png_simple(img_path, out_path):
    """Simple JPG -> PNG conversion (no background removal, for tiles)."""
    img = Image.open(img_path).convert('RGBA')
    img.save(out_path)
    print(f"  Converted: {os.path.basename(img_path)} -> {os.path.basename(out_path)}")


# ── sprites that need background removal ──
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
        remove_checkerboard_bg(src_path, dst_path)
    else:
        print(f"  MISSING: {src}")

# ── duck_hurt.jpg has a dark background, needs different approach ──
print("\n=== Converting duck_hurt (dark background) ===")
hurt_src = os.path.join(PIXEL_ANIM, "duck_hurt.jpg")
hurt_dst = os.path.join(PIXEL_ANIM, "duck_hurt.png")
if os.path.exists(hurt_src):
    # duck_hurt has a very dark/black background
    img = Image.open(hurt_src).convert('RGBA')
    # Keep as-is since it's an explosion effect that fills the cell
    img.save(hurt_dst)
    print(f"  Converted: duck_hurt.jpg -> duck_hurt.png")

# ── FinishLine images have blue/teal backgrounds ──
print("\n=== Converting FinishLine (keep intact, has colored BG) ===")
for i in range(1, 4):
    src = os.path.join(PIXEL_ANIM, f"FinishLine_{i}.jpg")
    dst = os.path.join(PIXEL_ANIM, f"FinishLine_{i}.png")
    if os.path.exists(src):
        convert_to_png_simple(src, dst)

# ── tiles (no transparency needed) ──

print("\n=== Converting tiles (no transparency removal) ===")
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

# ── Convert sliced walk/idle frames too ──
print("\n=== Converting walk frames (with background removal) ===")
walk_dir = os.path.join(PIXEL_ANIM, "duck_walk_frames")
for f in sorted(os.listdir(walk_dir)):
    if f.endswith('.jpg') or f.endswith('.png'):
        src_path = os.path.join(walk_dir, f)
        dst_name = os.path.splitext(f)[0] + '.png'
        dst_path = os.path.join(walk_dir, dst_name)
        remove_checkerboard_bg(src_path, dst_path)

print("\n=== Converting idle frames (with background removal) ===")
idle_dir = os.path.join(PIXEL_ANIM, "duck_idle_frames")
for f in sorted(os.listdir(idle_dir)):
    if f.endswith('.png'):
        src_path = os.path.join(idle_dir, f)
        # Re-process in-place since they came from JPG sprite sheet
        remove_checkerboard_bg(src_path, src_path)

print("\nDone! All assets converted.")
