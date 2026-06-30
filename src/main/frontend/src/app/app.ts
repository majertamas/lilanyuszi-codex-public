import {Component, inject} from '@angular/core';
import {Router, RouterOutlet} from '@angular/router';
import {ButtonModule} from 'primeng/button';
import {UserService} from './service/user-service';
import {BlockUI} from 'primeng/blockui';
import {BlockUiService} from './service/block-ui.service';
import {Toast} from 'primeng/toast';
import {NgClass} from '@angular/common';
import {AuthService} from './service/auth-service';
import {MessageService} from 'primeng/api';
import {DarkModeService} from './service/dark-mode.service';
import {MButton} from './component/lib/m-button/m-button';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ButtonModule, BlockUI, Toast, NgClass, MButton],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  authService = inject(AuthService);
  userService = inject(UserService);
  blockUiService: BlockUiService = inject(BlockUiService);
  router: Router = inject(Router);
  messageService: MessageService = inject(MessageService);
  darkModeService: DarkModeService = inject(DarkModeService);

  logout() {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['login'])
    })
  }

  messageTest() {
    this.messageService.add({
      severity: 'error',
      summary: `Test`,
      detail: 'TEst',
      life: 5000
    });
  }

  toggleDarkMode() {
    this.darkModeService.toggleDarkMode();
  }

  getIconBySeverity(severity: "success" | "info" | "warn" | "secondary" | "contrast" | "error"): string {
    switch (severity.toLowerCase()) {
      case 'success':
        return 'check_circle';
      case 'warn':
        return 'warning';
      case 'error':
        return 'error';
      default:
        return 'info';
    }
  }

  getTextColorBySeverity(severity: "success" | "info" | "warn" | "secondary" | "contrast" | "error"): string {
    switch (severity.toLowerCase()) {
      case 'success':
        return 'p-green-600';
      case 'info':
        return 'p-blue-600';
      case 'warn':
        return 'p-yellow-600';
      case 'secondary':
        return 'p-surface-600';
      case 'contrast':
        return 'p-surface-50';
      case 'error':
        return 'p-red-600';
      default:
        return '';
    }
  }

  protected me() {
    this.router.navigate(['me']);
  }

  protected lists() {
    this.router.navigate(['lists']);
  }

  protected shoppingLists() {
    this.router.navigate(['shopping-lists']);
  }
}
