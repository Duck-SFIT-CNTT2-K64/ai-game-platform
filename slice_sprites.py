import os
from PIL import Image

def slice_spritesheet(img_path, rows, cols, out_dir, out_prefix):
    os.makedirs(out_dir, exist_ok=True)
    print(f"Slicing {img_path} into {rows}x{cols} grid...")
    img = Image.open(img_path)
    
    # If the image is JPEG, it might not have an alpha channel, and might have a solid background.
    # The user might want transparent backgrounds if it's a sprite. But for now, we just slice.
    # Optionally we can convert it to RGBA and try to remove the background, but the prompt just says "slice".
    if img.mode != 'RGBA':
        img = img.convert('RGBA')

    w, h = img.size
    frame_w = w // cols
    frame_h = h // rows
    
    count = 0
    for r in range(rows):
        for c in range(cols):
            box = (c * frame_w, r * frame_h, (c + 1) * frame_w, (r + 1) * frame_h)
            frame = img.crop(box)
            out_file = os.path.join(out_dir, f"{out_prefix}_{count:02d}.png")
            frame.save(out_file)
            print(f"Saved {out_file}")
            count += 1

base_path = r"d:\nam_3_ki_2\tri_tue_nhan_tao\ai-game-platform\src\main\resources\image"

# Duck_idle.jpg is 1 row, 4 cols
slice_spritesheet(
    os.path.join(base_path, r"pixel_animation\Duck_idle.jpg"),
    1, 4,
    os.path.join(base_path, r"pixel_animation\duck_idle_frames"),
    "idle"
)

# Duck_walk.jpg is 1 row, 4 cols
slice_spritesheet(
    os.path.join(base_path, r"pixel_animation\Duck_walk.jpg"),
    1, 4,
    os.path.join(base_path, r"pixel_animation\duck_walk_frames"),
    "walk"
)

# duck_walk_in_mainmenu.png is 2 rows, 4 cols
slice_spritesheet(
    os.path.join(base_path, r"pixel_MainMenu\duck_walk_in_mainmenu.png"),
    2, 4,
    os.path.join(base_path, r"pixel_MainMenu\duck_mainmenu_walk_frames"),
    "mm_walk"
)
