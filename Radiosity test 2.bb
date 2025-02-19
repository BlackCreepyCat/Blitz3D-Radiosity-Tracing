Const grw=400,grh=300

Graphics3D grw,grh,32,2
SetBuffer BackBuffer()

Const  RayCount=2000
Const  RenderCamX=156

Global Picture=CreateImage(grw,grh)
Global pr#,pg#,pb#

Type entity
	Field ent
	Field r#,g#,b#
End Type

Dim shade#(RenderCamX,RenderCamX)

cc=RenderCamX/2

For y=-cc To cc-1
	For x=-cc To cc-1
		lg#=Sqr(x*x+y*y)
		
		If lg<cc Then shade(x+cc,y+cc)=(1.0-(lg/cc))*6.0
	Next
Next

;================================================================================================

e1.entity=add_cube(0,0,20, 7,10,7, 15,15,15 , 1)
e1.entity=add_cube(0, 10,20, 2,0.2,2, 500,500,500 , 0)
e1.entity=add_cube(-7,0,20, 0.05,10,7, 20,0,0 , 0)
e1.entity=add_cube( 7,0,20, 0.05,10,7, 0,20,5 , 0)
e1.entity=add_cube( 3,-3,22, 2,7,2, 15,15,15 , 0) : TurnEntity e1\ent,0,-30,0
e1.entity=add_cube(-3,-6,17, 2,4,2, 10,10,1 , 0) : TurnEntity e1\ent,0, 30,0
s1.entity=add_sphere( 3,-7.5,17, 2.5, 10,3,1) 


AmbientLight 100,100,100

Global viewCam=CreatePivot()
PositionEntity viewCam,0,0,5
TurnEntity viewCam,0,0,0




Global RenderCam=CreateCamera(viewCam)
EntityParent RenderCam,0
CameraRange RenderCam,0.01,200
CameraViewport RenderCam, 0,0,RenderCamX,RenderCamX






FrmCur=0
FrmMax=5

For y=-grh*0.5 To grh*0.5-1 Step 1
	
	LockBuffer(ImageBuffer(Picture))
	
	For x=-grw*0.5 To grw*0.5-1 Step 1
		
		pr=0
		pg=0
		pb=0
		
		TFormPoint x,-y,grw*0.5,viewCam,0
		
		Ray (EntityX(viewCam),EntityY(viewCam),EntityZ(viewCam), TFormedX(),TFormedY(),TFormedZ())
		
		If pr>255 Then pr=255
		If pg>255 Then pg=255
		If pb>255 Then pb=255
		
		
		WritePixelFast x+grw*0.5,y+grh*0.5,pr Shl 16 + pg Shl 8 + pb, ImageBuffer(Picture)
		
	Next
	
	UnlockBuffer(ImageBuffer(Picture))
	
	FrmCur=FrmCur+1
	
	If FrmCur>FrmMax Then
		FrmCur=0 : Cls : DrawImage Picture,0,0:Flip False
	EndIf
	
	If KeyHit(1) Then Exit
	
Next

SaveImage Picture,"C:\_Output_.bmp"

WaitKey():End 


;================================================================================================

Function Ray(x1#,y1#,z1#, x2#,y2#,z2#)
	
	If LinePick (x1,y1,z1,  x2-x1,y2-y1,z2-z1) = 0 Return
		
		For ee.entity=Each entity
			If ee\ent = PickedEntity() Then
				
				PositionEntity RenderCam,PickedX(),PickedY(),PickedZ()
				AlignToVector  RenderCam,PickedNX(),PickedNY(),PickedNZ(),3
				
				RenderWorld()
				
				LockBuffer BackBuffer()   
				
				For cnt=0 To RayCount-1   
					
					cx=Rand(0,RenderCamX-1)
					cy=Rand(0,RenderCamX-1)            
					
					s#=shade(cx,cy)
					
					If s<>0
						
						col=ReadPixelFast(cx,cy,BackBuffer())
						
						pr=pr+((col Shr 16) And 255) * s
						pg=pg+((col Shr  8) And 255) * s
						pb=pb+((col       ) And 255) * s
						
					EndIf
					
				Next   
				
				UnlockBuffer BackBuffer()
				
				lx#=PickedX()-x1
				ly#=PickedY()-y1
				lz#=PickedZ()-z1
				
				lg#=2000/(lx*lx+ly*ly+lz*lz)
				
				pr=(ee\r + (pr/cnt)) * lg
				pg=(ee\g + (pg/cnt)) * lg
				pb=(ee\b + (pb/cnt)) * lg
				
				Exit
			EndIf
		Next
End Function

;================================================================================================

Function add_sphere.entity(x#,y#,z# ,rad#, r,g,b)
	e.entity=New entity
	e\ent=CreateSphere()
	e\r=r : e\g=g :   e\b=b      
	EntityPickMode e\ent,1
	PositionEntity e\ent,x,y,z
	EntityColor e\ent,r,g,b
	EntityRadius e\ent,rad
	ScaleEntity e\ent,rad,rad,rad
	
	Return e
End Function

;================================================================================================

Function add_cube.entity(x#,y#,z# ,xs#,ys#,zs#, r,g,b,  flip_)
	e.entity=New entity
	e\ent=CreateCube()
	e\r=r : e\g=g :   e\b=b
	
	If flip_=1 Then
		FlipMesh e\ent
		EntityPickMode e\ent,2
	Else
		EntityPickMode e\ent,3
	EndIf   
	
	EntityColor e\ent,r,g,b
	ScaleEntity e\ent,xs,ys,zs
	EntityBox e\ent,-xs,-ys,-zs,xs*2,ys*2,zs*2
	PositionEntity e\ent,x,y,z   
	Return e
End Function

;~IDEal Editor Parameters:
;~C#Blitz3D