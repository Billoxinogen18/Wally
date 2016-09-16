var HashSet = function(hashFunction){
  var hash = {};

  this.insert = function(val){
      hash[hashFunction(val)] = val;
  };

  this.remove = function(val){
      delete hash[hashFunction(val)]
  };

  this.contains = function(val){
      return hash[hashFunction(val)] ? true : false;

  };

  this.size = function(){
    return Object.keys(hash).length
  };

  this.toArray = function(){
      var dataArray = [];
      for(var o in hash) {
          dataArray.push(hash[o]);
      }
      return dataArray;
  }
};