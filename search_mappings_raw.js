const fs = require('fs');
const path = require('path');
function search(dir) {
    const files = fs.readdirSync(dir);
    for (const f of files) {
        const p = path.join(dir, f);
        if (fs.statSync(p).isDirectory()) search(p);
        else if (f.endsWith('.java')) {
            const txt = fs.readFileSync(p, 'utf8');
            if (txt.includes('@RestController') || txt.includes('@Controller')) {
                const lines = txt.split('\n');
                lines.forEach(line => {
                    if (line.includes('Mapping') && (line.includes('members') || line.includes('trainers'))) {
                        console.log(p, "->", line.trim());
                    }
                });
            }
        }
    }
}
search('C:/Users/nguye/Downloads/backend_fitlife/src');
