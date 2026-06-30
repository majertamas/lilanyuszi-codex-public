import {Component, inject, OnInit} from '@angular/core';
import {Button} from "primeng/button";
import {UserService} from '../../service/user-service';
import {AuthService} from '../../service/auth-service';

@Component({
  selector: 'app-login',
    imports: [
        Button
    ],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {

  userService = inject(UserService);
  authService = inject(AuthService);

  ngOnInit() {
    this.authService.logout().subscribe();
  }

  gmail() {
    globalThis.location.href = `http://localhost:8080/oauth2/authorization/google`;
  }

}
