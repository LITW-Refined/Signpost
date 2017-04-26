package gollorum.signpost.render;

import net.minecraft.client.model.ModelRenderer;

public class BigBoard {
	
	private ModelRenderer baseNotFlipped;
	private ModelRenderer baseFlipped;

	public BigBoard(ModelBigPost modelBigPost) {
		
		//Not flipped
		ModelRenderer ltnf = new ModelRenderer(modelBigPost, 15, 0);
		ltnf.addBox(-9, 2, 2, 1, 10, 1, 0f);
		baseNotFlipped = new ModelRenderer(modelBigPost, -1, 15);
		baseNotFlipped.addBox(-8, 1, 2, 19, 12, 1, 0.0F);
		baseNotFlipped.addChild(ltnf);

    	float i = 0.5f;
    	int hv = 0;
    	while(i<3.5){
    		ModelRenderer now = new ModelRenderer(modelBigPost, 15 , hv);
        	hv += 2;
        	now.addBox(10.5f+i, 1+i*2, 2, 1, (int)(12-4*i), 1, 0f);
    		baseNotFlipped.addChild(now);
        	i++;
    	}
		
		//flipped
		ModelRenderer ltf = new ModelRenderer(modelBigPost, 15, 0);
		ltf.addBox(8, 2, 2, 1, 10, 1, 0f);
		baseFlipped = new ModelRenderer(modelBigPost, -1, 15);
		baseFlipped.addBox(-10, 1, 2, 18, 12, 1, 0.0F);
		baseFlipped.addChild(ltf);
		
    	i = 0.5f;
    	hv = 0;
    	while(i<3.5){
    		ModelRenderer now = new ModelRenderer(modelBigPost, 15 , hv);
        	hv += 2;
        	now.addBox(-10.5f-i, 1+i*2, 2, 1, (int)(12-4*i), 1, 0f);
    		baseFlipped.addChild(now);
        	i++;
    	}
	}

	public void render(float f5, boolean flipped) {
		if(!flipped){
			baseFlipped.render(f5);
		}else{
			baseNotFlipped.render(f5);
		}
	}

    public void setRotation(float rot){
    	baseNotFlipped.rotateAngleY = baseFlipped.rotateAngleY = (float) Math.toRadians(rot);
    }
    
    public void setTextureOffset(int u, int v){
    	baseNotFlipped.setTextureOffset(u, v);
    	baseFlipped.setTextureOffset(u, v);
    }
    
    public void setTextureSize(int u, int v){
    	baseNotFlipped.setTextureSize(u, v);
    	baseFlipped.setTextureSize(u, v);
    }
}