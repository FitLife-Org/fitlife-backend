const fs = require('fs');
const path = require('path');
function search(dir) {
    const files = fs.readdirSync(dir);
    for (const f of files) {
        const p = path.join(dir, f);
        if (fs.statSync(p).isDirectory()) search(p);
        else if (f.endsWith('Controller.java')) {
            const txt = fs.readFileSync(p, 'utf8');
            let classMapping = '';
            const cmMatch = /@RequestMapping\((?:value\s*=\s*)?\"([^\"]+)\"\)/.exec(txt);
            if (cmMatch) classMapping = cmMatch[1];
            
            const lines = txt.split('\n');
            lines.forEach(line => {
                if (line.includes('Mapping')) {
                    console.log(p.replace('C:\\Users\\nguye\\Downloads\\backend_fitlife\\src\\main\\java\\com\\fitlife\\', ''), "->", line.trim());
                }
            });
        }
    }
}
search('C:/Users/nguye/Downloads/backend_fitlife/src');
