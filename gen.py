import json,math
S=11;I=4.5;O=5.5
a=["tachyon:singularity_casing","tachyon:singularity_port","tachyon:exotic_matter_core","tachyon:singularity_controller"]
s={};r={};l={};ts=0;ta=0
for y in range(S):
 v=y-5;p=[];q=[]
 for x in range(-5,6):
  for z in range(-5,6):
   d=math.sqrt(x*x+v*v+z*z)
   if I<=d<=O:p.append([x,z])
   elif d<I:q.append([x,z])
 s[str(y)]=p
 if q:r[str(y)]=q
 l[str(y)]={"accepts":a}
 print("Layer %2d (y=%+d): %3d shell, %3d air"%(y,v,len(p),len(q)))
 ts+=len(p);ta+=len(q)
print("Total shell: %d"%ts)
print("Total air: %d"%ta)
o={"controller_offset":[-5,0,0],"height":S,"shell":s,"air":r,"layers":l,"required_blocks":{"tachyon:exotic_matter_core":1}}
f=open("d:/MCMODDING/tachyon/src/main/resources/data/tachyon/multiblock/singularity_engine.json","w")
json.dump(o,f,indent=2)
f.close()
print("Written OK")
