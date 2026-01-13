"""
批量压缩 3D 图标图片
将 PNG 图片压缩到合适大小以提升性能
"""
from PIL import Image
import os

def compress_image(input_path, output_path, max_size_kb=200):
    """
    压缩图片到指定大小
    :param input_path: 输入文件路径
    :param output_path: 输出文件路径
    :param max_size_kb: 目标文件大小（KB）
    """
    img = Image.open(input_path)
    
    # 转换为 RGBA（保留透明度）
    if img.mode != 'RGBA':
        img = img.convert('RGBA')
    
    # 计算合适的尺寸（图标实际显示大小是 80dp，对应约 240-320px）
    # 保持宽高比，最大边不超过 512px
    max_dimension = 512
    width, height = img.size
    if width > max_dimension or height > max_dimension:
        if width > height:
            new_width = max_dimension
            new_height = int(height * (max_dimension / width))
        else:
            new_height = max_dimension
            new_width = int(width * (max_dimension / height))
        img = img.resize((new_width, new_height), Image.Resampling.LANCZOS)
        print(f"  调整尺寸: {width}x{height} -> {new_width}x{new_height}")
    
    # 尝试不同的质量级别来达到目标大小
    quality = 95
    while quality > 50:
        img.save(output_path, 'PNG', optimize=True, compress_level=9)
        
        # 检查文件大小
        size_kb = os.path.getsize(output_path) / 1024
        
        if size_kb <= max_size_kb or quality <= 50:
            print(f"  压缩完成: {size_kb:.2f} KB (质量: {quality})")
            break
        
        # 如果还是太大，进一步缩小尺寸
        if quality == 95 and size_kb > max_size_kb * 2:
            current_width, current_height = img.size
            img = img.resize((int(current_width * 0.8), int(current_height * 0.8)), 
                           Image.Resampling.LANCZOS)
            print(f"  进一步缩小尺寸: {current_width}x{current_height} -> {img.size[0]}x{img.size[1]}")
        
        quality -= 5

def main():
    drawable_path = r"d:\universalbox\app\src\main\res\drawable"
    
    # 需要压缩的图片列表
    images = [
        'img_3d_clock.png',
        'img_3d_decision.png',
        'img_3d_ocr.png',
        'img_3d_qrcode.png',
        'img_3d_tomatoclock.png'
    ]
    
    print("开始压缩图片...\n")
    
    for img_name in images:
        input_path = os.path.join(drawable_path, img_name)
        
        if not os.path.exists(input_path):
            print(f"⚠️  文件不存在: {img_name}")
            continue
        
        # 获取原始文件大小
        original_size = os.path.getsize(input_path) / 1024
        print(f"📷 {img_name}")
        print(f"  原始大小: {original_size:.2f} KB")
        
        # 备份原文件
        backup_path = input_path.replace('.png', '_original.png')
        if not os.path.exists(backup_path):
            os.rename(input_path, backup_path)
            print(f"  已备份到: {os.path.basename(backup_path)}")
            input_path = backup_path
        else:
            # 如果备份已存在，使用备份作为输入
            input_path = backup_path
            print(f"  使用备份文件: {os.path.basename(backup_path)}")
        
        # 压缩图片
        compress_image(input_path, os.path.join(drawable_path, img_name), max_size_kb=250)
        
        # 显示压缩结果
        new_size = os.path.getsize(os.path.join(drawable_path, img_name)) / 1024
        reduction = (1 - new_size / original_size) * 100
        print(f"  压缩率: {reduction:.1f}%\n")
    
    print("✅ 所有图片压缩完成！")
    print("\n💡 提示：原始文件已备份为 *_original.png，如需恢复可以重命名回来。")

if __name__ == '__main__':
    main()
