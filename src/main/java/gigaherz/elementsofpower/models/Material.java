package gigaherz.elementsofpower.models;

import javax.vecmath.Vector3f;

/**
 * Created by gigaherz on 02/01/2015.
 */
public class Material {

    public String Name;

    public Vector3f AmbientColor;
    public Vector3f DiffuseColor;
    public Vector3f SpecularColor;
    public float SpecularCoefficient;

    public float Transparency;

    public int IlluminationModel;

    public String AmbientTextureMap;
    public String DiffuseTextureMap;

    public String SpecularTextureMap;
    public String SpecularHighlightTextureMap;

    public String BumpMap;
    public String DisplacementMap;
    public String StencilDecalMap;

    public String AlphaTextureMap;

    public Material(String materialName)
    {
        Name = materialName;
    }

}
