import {Injectable} from '@angular/core';
import {RestClientService} from './rest.client.service';
import {Group, GroupAliasRequest} from '../model/group.model';

@Injectable({providedIn: 'root'})
export class GroupAliasService extends RestClientService {

  private readonly apiUrl: string = '/api/shared-access-aliases';

  public saveAlias(request: GroupAliasRequest) {
    return this.post<void>(this.apiUrl, request);
  }

}
