import {
  Component,
  effect,
  inject,
  input,
  InputSignal,
  output,
  OutputEmitterRef,
  signal,
  WritableSignal
} from '@angular/core';
import {DarkModeService} from '../../../service/dark-mode.service';
import {MButton} from '../m-button/m-button';

@Component({
  selector: 'app-m-card',
  imports: [
    MButton
  ],
  templateUrl: './m-card.html',
  styleUrl: './m-card.css',
})
export class MCard {

  classes: WritableSignal<string> = signal<string>('');
  iconClasses: WritableSignal<string> = signal<string>('');
  darkClasses: InputSignal<string> = input<string>('');
  lightClasses: InputSignal<string> = input<string>('');
  darkAndLightClasses: InputSignal<string> = input<string>('');
  icon: InputSignal<string> = input<string>('');
  iconLightClasses: InputSignal<string> = input<string>('');
  iconDarkClasses: InputSignal<string> = input<string>('');
  title: InputSignal<string> = input.required<string>();
  darkModeService: DarkModeService = inject(DarkModeService);
  onClick: OutputEmitterRef<void> = output<void>();

  constructor() {
    effect((): void => {
      if (this.darkModeService.isDarkMode()) {
        this.classes.set(this.darkClasses() + ' ' + this.darkAndLightClasses());
        this.iconClasses.set(this.iconDarkClasses());
      } else {
        this.classes.set(this.lightClasses() + ' ' + this.darkAndLightClasses());
        this.iconClasses.set(this.iconLightClasses());
      }
    });
  }

}
