var _MULE_INCLUDE = {

  //ugly patch to IE
  script: function(libraryName) {
    document.write('<script type="text/javascript" src="'+libraryName+'"></script>');
  },
  load: function() {
    var scriptTags = document.getElementsByTagName("script");
    for(var i=0;i<scriptTags.length;i++) {
      if(scriptTags[i].src && scriptTags[i].src.match(/mule\.js$/)) {
        var path = scriptTags[i].src.replace(/mule\.js$/,'').replace("js/my","mule-resource/js/");
        this.script(path + 'dojo/dojo.js');
        this.script('js/_mymule.js');
        break;
      }
    }
  }
}

_MULE_INCLUDE.load();