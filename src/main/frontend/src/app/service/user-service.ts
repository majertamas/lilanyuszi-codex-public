import {inject, Injectable, signal} from '@angular/core';
import {User} from '../model/user.model';
import {tap} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {RestClientService} from './rest.client.service';

@Injectable({
  providedIn: 'root',
})
export class UserService extends RestClientService {

  http = inject(HttpClient);
  user = signal<User | null>(null);

  me() {
    return this.get<User>('/api/user/me').pipe(
      tap(user => {
        console.log(user)
        this.user.set(user);
      })
    );
  }

  isLoggedIn(): boolean {
    return this.user() !== null;
  }

  addOrModifyNickName(nickname: string) {
    return this.http.put<User>('/api/user/me/nickname', {nickname}).pipe(
      tap(user => {
        this.user.set(user);
      })
    );
  }

  addOrModifyPicture(picture: string) {
    console.log('pictureUrl', picture)
    return this.http.put<User>('/api/user/me/picture', {picture}).pipe(
      tap(user => {
        this.user.set(user);
      })
    );
  }
}
