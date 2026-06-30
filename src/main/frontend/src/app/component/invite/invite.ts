import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {InviteService} from '../../service/invite-service';

@Component({
  selector: 'app-invite',
  imports: [],
  templateUrl: './invite.html',
  styleUrl: './invite.css',
})
export class Invite implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly inviteService = inject(InviteService);

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      this.inviteService.acceptInvitation(token).subscribe();
    });
  }

}
