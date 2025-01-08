var inf = 1e-6;

function dist(p1, p2) {
  return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
}


function getCycle(p1, p2, r) {
  let mid = {
    x: (p1.x + p2.x) / 2,
    y: (p1.y + p2.y) / 2
  };
  let angle = Math.atan2(p1.x - p2.x, p2.y - p1.y);
  let d = Math.sqrt(r * r - Math.pow(dist(p1, mid), 2));
  mid = {
    x: mid.x + d * Math.cos(angle),
    y: mid.y + d * Math.sin(angle)
  };
  return mid;
}

function getMax(r, count, p) {
  let num = p.length;
  let a, b;
  let eps = 1e-8;
  let i, j;
  let ans = 0;
  let final_res = [];

  for (let i = 0; i < num; i++) {
    for (j = i + 1; j < num; j++) {
      if (dist(p[i], p[j]) > 2.0 * r)
        continue;
      let center = getCycle(p[i], p[j], r);
      let x = 0,
        y = 0;
      let cnt = 0;
      for (let k = 0; k < num; k++)
        if (dist(center, p[k]) < r + eps) {
          let point = p[k];
          x = x + point.x;
          y = y + point.y;
          cnt++;
        }

      ans = Math.max(ans, cnt);
      if (ans == cnt) {

        // let finalsize=tagid_queue.size();
        if (cnt != 0) {
          var xx = x / cnt;
          var yy = y / cnt;
          final_res = {
            center_x: xx,
            center_y: yy
          };


        }
      }
    }
  }


  if (ans >= count) {
    final_res.hasRes = true;
  } else {
    final_res.hasRes = false;
  }
  return final_res;
}

//经纬度转墨卡托
function lonLat2Mercator(lng, lat) {
  var x = lng * 20037508.34 / 180;
  var y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
  y = y * 20037508.34 / 180;
  var xy = [x, y];
  // System.out.println("xy"+xy[0]+"xy"+xy[1]);
  return xy;
}

//墨卡托转经纬度
function Mercator2lonLat(X, Y) {

  var x = X / 20037508.34 * 180;
  var y = Y / 20037508.34 * 180;
  y = 180 / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180)) - Math.PI / 2);
  var lnglat = [x, y];
  //System.out.println("lnglat:"+x+"lnglat:"+y);
  return lnglat;

}

function contains(arr, val) {
  return arr.some(item => item === val);
}
