export function log(myVar: any, message?: string, c = 'cyan'): void {
  const m: string = message || 'Log from util.ts';
  console.log(`%c ${m}`, `color: ${c}; font-weight: bold;`, myVar);
}
