import {Injectable} from '@angular/core';
import {RestClientService} from '../../service/rest.client.service';
import {Group, GroupMemberResponse, GroupNameRequest} from '../../model/group.model';
import {Observable} from 'rxjs';
import {ModifyGroupName} from '../../service/modify-group-name.interface';
import {ShoppingListResponse} from './shopping-list.model';

@Injectable({
  providedIn: 'root',
})
export class ShoppingListService extends RestClientService implements ModifyGroupName {

  private readonly apiUrl: string = '/api/shopping-lists';

  public getShoppingListsWhereMember(): Observable<Group[]> {
    return this.get<Group[]>(this.apiUrl);
  }

  public modifyGroupName(request: GroupNameRequest): Observable<Group> {
    return this.put<Group>(this.apiUrl, request);
  }

  public deleteShoppingList(shoppingListId: number) {
    return this.delete<void>(`${this.apiUrl}/${shoppingListId}`);
  }

  public leaveShoppingList(shoppingListId: number) {
    return this.delete<void>(`${this.apiUrl}/${shoppingListId}/members/me`);
  }

  public removeMember(shoppingList: Group, member: GroupMemberResponse) {
    return this.delete<Group[]>(`${this.apiUrl}/remove-member/${shoppingList.id}/${member.userId}`);
  }

  public createNew(name: string): Observable<ShoppingListResponse> {
    console.log('name: ', name)
    return this.post<ShoppingListResponse>(this.apiUrl, {name});
  }

}
