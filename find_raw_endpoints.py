import os
import re

base_dir = r"C:\Users\nguye\Downloads\backend_fitlife\src\main\java\com\fitlife"

mapping_regex = re.compile(r'@(Get|Post|Put|Patch|Delete)Mapping')
method_regex = re.compile(r'public\s+(?!ResponseEntity)(?!ApiResponse)([\w<>,?\s\[\]]+)\s+(\w+)\s*\(')

issues = []

for root, _, files in os.walk(base_dir):
    for file in files:
        if file.endswith("Controller.java"):
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
                
            # split by mapping
            blocks = mapping_regex.split(content)
            # blocks[0] is everything before first mapping. 
            # elements are mapped as: blocks[0], match1, blocks[2], match2, blocks[4]...
            for i in range(2, len(blocks), 2):
                block = blocks[i]
                # look for method declaration
                m = method_regex.search(block)
                if m:
                    return_type = m.group(1).strip()
                    method_name = m.group(2)
                    issues.append(f"{file} -> {method_name} returns {return_type}")

for issue in issues:
    print(issue)
if not issues:
    print("All controllers seem to return ApiResponse or ResponseEntity.")
