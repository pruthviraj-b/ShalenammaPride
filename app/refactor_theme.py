import os
import re

screens_dir = r"d:\ShalenammaPride\app\src\main\java\com\pruthviraj\shalenammapride\screens"

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Colors
    content = re.sub(r'val\s+\w+\s*=\s*if\s*\(isDark\)\s*Color\([^)]+\)\s*else\s*Color\([^)]+\)', 'val BgLight = MaterialTheme.colorScheme.background', content) # Generic fallback, let's be more specific
    
    # Specific variable names
    content = re.sub(r'val\s+NavyPrimary\s*=\s*.*', 'val NavyPrimary = MaterialTheme.colorScheme.primary', content)
    content = re.sub(r'val\s+NavySecondary\s*=\s*.*', 'val NavySecondary = MaterialTheme.colorScheme.secondary', content)
    content = re.sub(r'val\s+BgLight\s*=\s*.*', 'val BgLight = MaterialTheme.colorScheme.background', content)
    content = re.sub(r'val\s+TextPrim\s*=\s*.*', 'val TextPrim = MaterialTheme.colorScheme.onBackground', content)
    content = re.sub(r'val\s+TextMuted\s*=\s*.*', 'val TextMuted = MaterialTheme.colorScheme.onSurfaceVariant', content)
    content = re.sub(r'val\s+Border\s*=\s*.*', 'val Border = MaterialTheme.colorScheme.outline', content)
    content = re.sub(r'val\s+SurfaceColor\s*=\s*.*', 'val SurfaceColor = MaterialTheme.colorScheme.surface', content)

    # Any remaining hardcoded `if (isDark) Color(...) else Color(...)`
    # Mostly used as background or surface.
    content = re.sub(r'if\s*\(isDark\)\s*Color\(0xFF111111\)\s*else\s*Color\.White', 'MaterialTheme.colorScheme.surface', content)
    content = re.sub(r'if\s*\(isDark\)\s*Color\(0xFF1E293B\)\s*else\s*Color\(0xFFEFF6FF\)', 'MaterialTheme.colorScheme.surfaceVariant', content)
    content = re.sub(r'if\s*\(isDark\)\s*Color\(0xFF1E293B\)\s*else\s*Color\(0xFFF9FAFB\)', 'MaterialTheme.colorScheme.surface', content)
    content = re.sub(r'if\s*\(isDark\)\s*Color\(0xFF1E293B\)\s*else\s*Color\.White', 'MaterialTheme.colorScheme.surface', content)
    content = re.sub(r'if\s*\(isDark\)\s*Color\(0xFF[0-9A-Fa-f]+\)\s*else\s*Color\(0xFF[0-9A-Fa-f]+\)', 'MaterialTheme.colorScheme.surface', content)

    # Replace hardcoded blues like Color(0xFF3B82F6) with primary
    content = re.sub(r'Color\(0xFF3B82F6\)', 'MaterialTheme.colorScheme.primary', content)
    
    # Typography mapping
    # Large headers
    content = re.sub(r'fontSize\s*=\s*2[48]\.sp,\s*fontWeight\s*=\s*FontWeight\.(ExtraBold|Bold)', 'style = MaterialTheme.typography.headlineLarge', content)
    content = re.sub(r'fontSize\s*=\s*2[02]\.sp,\s*fontWeight\s*=\s*FontWeight\.Bold', 'style = MaterialTheme.typography.titleLarge', content)
    
    # Body text
    content = re.sub(r'fontSize\s*=\s*1[456]\.sp,\s*fontWeight\s*=\s*FontWeight\.(Normal|Medium)', 'style = MaterialTheme.typography.bodyLarge', content)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for filename in os.listdir(screens_dir):
    if filename.endswith(".kt"):
        process_file(os.path.join(screens_dir, filename))

print("Done")
