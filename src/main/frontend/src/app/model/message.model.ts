export interface Message {
  text: string;
  severity: 'INFO' | 'SUCCESS' | 'WARN' | 'ERROR' | 'SECONDARY' | 'CONTRAST';
}
