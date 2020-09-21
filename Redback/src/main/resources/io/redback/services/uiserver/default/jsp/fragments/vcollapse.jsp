<% 
var showExpr = true;
if(typeof parents.dataset != 'undefined') {
	showExpr = (config.show != null ? config.show : 'true').split('object').join(parents.dataset + '.selectedObject').split('relatedObject').join(parents.dataset + '.relatedObject');
	if(showExpr.indexOf('.selectedObject.') > -1) showExpr = parents.dataset + '.selectedObject != null && (' + showExpr + ')';
	if(showExpr.indexOf('.relatedObject.') > -1) showExpr = parents.dataset + '.relatedObject != null && (' + showExpr + ')';
}
%><rb-vcollapse
	*ngIf="<%=showExpr%>">
	#content#
</rb-vcollapse>