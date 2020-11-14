<rb-actiongroup
	[dataset]="<%=parents.dataset%>"
	[actions]="<%=utils.convertDataEntityToAttributeString(config.actions)%>"
	[round]="<%=config.round%>"
	[domaincategory]="'<%=config.domaincategory%>'">
</rb-actiongroup>