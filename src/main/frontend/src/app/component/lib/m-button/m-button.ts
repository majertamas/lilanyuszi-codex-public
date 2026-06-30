import {Component, input, InputSignal, output, OutputEmitterRef} from '@angular/core';
import {Button} from 'primeng/button';

@Component({
  selector: 'app-m-button',
  imports: [
    Button
  ],
  templateUrl: './m-button.html',
  styleUrl: './m-button.css',
})
export class MButton {
  label: InputSignal<string> = input<string>('');
  severity: InputSignal<'success' | 'info' | 'warn' | 'danger' | 'help' | 'primary' | 'secondary' | 'contrast'> =
    input<'success' | 'info' | 'warn' | 'danger' | 'help' | 'primary' | 'secondary' | 'contrast'>('contrast');
  size: InputSignal<'small' | 'large' | undefined> = input<'small' | 'large' | undefined>(undefined);
  icon: InputSignal<string | undefined> = input<string>();
  styleClass: InputSignal<string | undefined> = input<string>();
  iconStyleClass: InputSignal<string | undefined> = input<string>();
  variant: InputSignal< "outlined" | "text" | undefined> = input< "outlined" | "text" | undefined>(undefined);
  rounded: InputSignal<boolean> = input<boolean>(true);

  onClick: OutputEmitterRef<void> = output<void>();
}
