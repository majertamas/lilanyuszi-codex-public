import {Injectable, signal, WritableSignal} from '@angular/core';
import {DARK_MODE_CSS_SELECTOR, DARK_MODE_STORAGE_KEY} from '../util/constants';

@Injectable({
  providedIn: 'root',
})
export class DarkModeService {

  isDarkMode: WritableSignal<boolean> = signal<boolean>(false);

  setDarkModeByLocalStorage(): void {
    const darkMode: string | null = localStorage.getItem(DARK_MODE_STORAGE_KEY);
    const element: HTMLElement | null = document.querySelector('html');
    if (darkMode === 'true') {
      element!.classList.add(DARK_MODE_CSS_SELECTOR);
      this.isDarkMode.set(true);
    } else {
      element!.classList.remove(DARK_MODE_CSS_SELECTOR);
      this.isDarkMode.set(false);
    }
  }

  toggleDarkMode(): void {
    const element: HTMLElement | null = document.querySelector('html');
    element!.classList.toggle(DARK_MODE_CSS_SELECTOR);
    this.isDarkMode.set(!this.isDarkMode());
    localStorage.setItem(DARK_MODE_STORAGE_KEY, `${this.isDarkMode()}`);
  }

}
