const fs = require('fs');
const path = require('path');
function search(dir) {
    const files = fs.readdirSync(dir);
    for (const f of files) {
        const p = path.join(dir, f);
        if (fs.statSync(p).isDirectory()) search(p);
        else if (f.endsWith('.java')) {
            const txt = fs.readFileSync(p, 'utf8');
            if (txt.includes('getMyMembers') || txt.includes('getMembers')) {
                console.log(p);
            }
        }
    }
}
search('C:/Users/nguye/Downloads/backend_fitlife/src');
