from PIL import Image
import os

drawable_path = r"d:\universalbox\app\src\main\res\drawable"
input_file = os.path.join(drawable_path, "img_3d_clock_original.png")
output_file = os.path.join(drawable_path, "img_3d_clock.png")

img = Image.open(input_file)
if img.mode != 'RGBA':
    img = img.convert('RGBA')

# 调整尺寸
width, height = img.size
if width > 512 or height > 512:
    max_dim = 512
    if width > height:
        new_width = max_dim
        new_height = int(height * (max_dim / width))
    else:
        new_height = max_dim
        new_width = int(width * (max_dim / height))
    img = img.resize((new_width, new_height), Image.Resampling.LANCZOS)

img.save(output_file, 'PNG', optimize=True, compress_level=9)
size_kb = os.path.getsize(output_file) / 1024
print(f"压缩完成: {size_kb:.2f} KB")
