import {inject, Injectable} from '@angular/core';
import {RestClientService} from './rest.client.service';
import {InviteRequest} from '../model/invite-request.model';
import {UserService} from './user-service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class InviteService extends RestClientService {

  private readonly apiUrl: string = '/api/invite';
  userService = inject(UserService);

  addUserToGroup(groupId: number, email: string): Observable<void> {
    const body: InviteRequest = {email, inviterEmail: this.userService.user()!.email, groupId};
    return this.post<void>(this.apiUrl, body);
  }

  acceptInvitation(token: string): Observable<void> {
    return this.post<void>(`${this.apiUrl}/accept`, null, {token});
  }

}
