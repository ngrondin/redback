<md-input-container 
	class="md-block rb-search-container" >
	<md-icon class="md-hue-3"> search </md-icon>
	<input 
		ng-model="searchText" 
		ng-change="search(searchText)" 
		aria-label="Search"
		size="15">
</md-input-container>