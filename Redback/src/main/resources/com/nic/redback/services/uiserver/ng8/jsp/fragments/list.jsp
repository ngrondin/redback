<rb-list-scroll 
	style="<%=config.getString('inlineStyle')%>">
	<mat-action-list>
		<button
			mat-list-item 
			*ngFor="let item of <%=dataset%>.list" 
			[ngClass]="<%=dataset%>.selectedObject == item ? 'rb-list-item-active' : ''"
			(click)="<%=dataset%>.select(item)">
			<div 
				class="rb-list-item"
				layout=row><%
if(config.get('initials') != null) {%>
				<div
					class="rb-list-item-circle">
					<span class="rb-list-item-initials"><%=config.getString('initials')%></span>
				</div><%
}%>
				<div
					layout="column">
					<span
						class="rb-list-line1">
						<%=config.getString('line1')%>
					</span><br/>
					<span
						class="rb-list-line2">
						<%=config.getString('line2')%>
					</span>
				</div>
			</div>
		</button>
	</mat-action-list>
	<div
	    class="rb-spinner-container"
	    *ngIf="<%=dataset%>.isLoading">
	    <mat-spinner
	        diameter="20">
	    </mat-spinner>
	</div>	
</rb-list-scroll>