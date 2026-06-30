import {Component, inject, OnInit} from '@angular/core';
import {User} from '../../model/user.model';
import {NgOptimizedImage} from '@angular/common';
import {InputText} from 'primeng/inputtext';
import {FloatLabel} from 'primeng/floatlabel';
import {FormsModule} from '@angular/forms';
import {UserService} from '../../service/user-service';
import {InviteService} from '../../service/invite-service';

@Component({
  selector: 'app-me',
  imports: [
    NgOptimizedImage,
    InputText,
    FloatLabel,
    FormsModule
  ],
  templateUrl: './me.html',
  styleUrl: './me.css',
})
export class Me implements OnInit {

  userService = inject(UserService);
  inviteService = inject(InviteService);
  nickname: string = '';
  picture: string = '';
  protected email: string = '';

  ngOnInit(): void {
    this.userService.me().subscribe({
      next: (resp: User): void => {
        this.nickname = resp.nickName ?? '';
        this.picture = resp.picture ?? '';
      }
    })
  }

  protected putName() {
    this.userService.addOrModifyNickName(this.nickname).subscribe();
  }

  protected putPicture() {
    this.userService.addOrModifyPicture(this.picture).subscribe();
  }

  protected addToGroup(groupId: number) {
    this.inviteService.addUserToGroup(groupId, this.email).subscribe();
  }
}
